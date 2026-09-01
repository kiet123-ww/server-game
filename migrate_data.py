import re
import csv
import io
import os
import pymongo
from collections import defaultdict

def get_sql_path():
    candidates = [
        r'D:\NRO\SRC NRO NEW 01\ngocrong.sql',
        r'D:\NRO\Source-Nro\docs\ngocrong.sql',
        os.path.join(os.path.dirname(__file__), 'docs', 'ngocrong.sql'),
        os.path.join(os.path.dirname(__file__), 'ngocrong.sql'),
    ]
    for p in candidates:
        if os.path.exists(p):
            return p
    return 'ngocrong.sql'

def normalize_col_name(table_name, col):
    col_clean = col.strip().strip('`')
    if table_name in ['player', 'account']:
        if col_clean.upper() in ['NAME', 'TYPE', 'DATA']:
            return col_clean.lower()
        return col_clean
    else:
        return col_clean.lower()

def extract_schemas(sql):
    table_schema = {}
    create_table_pattern = re.compile(r"CREATE TABLE `([^`]+)` \((.*?)\) ENGINE", re.DOTALL)
    for match in create_table_pattern.finditer(sql):
        table_name = match.group(1).lower()
        table_body = match.group(2)
        col_types = {}
        for line in table_body.splitlines():
            line = line.strip()
            col_match = re.match(r"^`([^`]+)`\s+([a-zA-Z]+)", line)
            if col_match:
                raw_col = col_match.group(1)
                col_name = normalize_col_name(table_name, raw_col)
                col_type = col_match.group(2).lower()
                col_types[col_name] = col_type
        table_schema[table_name] = col_types
    return table_schema

def parse_sql_to_mongo():
    client = pymongo.MongoClient('mongodb://127.0.0.1:27017/')
    db = client['nro_db']
    
    sql_path = get_sql_path()
    print(f"Reading SQL file from: {sql_path}")
    with open(sql_path, 'r', encoding='utf-8') as f:
        sql = f.read()
        
    schemas = extract_schemas(sql)
    pattern = re.compile(r"INSERT INTO `([^`]+)` \(([^)]+)\) VALUES\s*(.*?);", re.DOTALL)
    
    table_data = defaultdict(list)
    
    for match in pattern.finditer(sql):
        table_name = match.group(1).lower()
        raw_columns = [c.strip().strip('`') for c in match.group(2).split(',')]
        columns = [normalize_col_name(table_name, c) for c in raw_columns]
        values_str = match.group(3).strip()
        
        if values_str.startswith('('):
            values_str = values_str[1:]
        if values_str.endswith(')'):
            values_str = values_str[:-1]
            
        rows = re.split(r'\)\s*,\s*\(', values_str)
        tbl_schema = schemas.get(table_name, {})
        
        for row in rows:
            # Fix escaped newlines BEFORE CSV parsing
            row = row.replace('\\r', '\r').replace('\\n', '\n')
            
            reader = csv.reader(io.StringIO(row), quotechar="'", escapechar="\\", skipinitialspace=True)
            try:
                parsed_row = next(reader)
            except StopIteration:
                continue
                
            doc = {}
            for i, col in enumerate(columns):
                if i < len(parsed_row):
                    val = parsed_row[i]
                    if val.upper() == 'NULL':
                        doc[col] = None
                        continue

                    col_type = tbl_schema.get(col, '')
                    if col in ['password', 'username', 'name', 'slogan', 'detail', 'notify', 'text']:
                        doc[col] = val
                    elif col_type in ['int', 'tinyint', 'smallint', 'mediumint', 'bigint']:
                        try:
                            doc[col] = int(val)
                        except ValueError:
                            doc[col] = val
                    elif col_type in ['float', 'double', 'decimal']:
                        try:
                            doc[col] = float(val)
                        except ValueError:
                            doc[col] = val
                    elif col_type in ['varchar', 'char', 'text', 'mediumtext', 'longtext', 'json']:
                        doc[col] = val
                    else:
                        if val.lstrip('-').isdigit():
                            doc[col] = int(val)
                        else:
                            doc[col] = val
            table_data[table_name].append(doc)
            
    for table_name, docs in table_data.items():
        if docs:
            db[table_name].delete_many({})
            db[table_name].insert_many(docs)
            print(f"Migrated {len(docs)} rows into collection '{table_name}'")

    # Update auto-increment counters based on migrated data
    max_account_id = max([d['id'] for d in table_data.get('account', []) if d.get('id') is not None], default=0)
    max_player_id = max([d['id'] for d in table_data.get('player', []) if d.get('id') is not None], default=0)
    db['counters'].update_one({'_id': 'accountId'}, {'$set': {'sequence_value': max_account_id}}, upsert=True)
    db['counters'].update_one({'_id': 'playerId'}, {'$set': {'sequence_value': max_player_id}}, upsert=True)
    print(f"Initialized counters: accountId={max_account_id}, playerId={max_player_id}")
            
if __name__ == '__main__':
    parse_sql_to_mongo()
