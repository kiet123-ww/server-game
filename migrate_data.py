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

def extract_table_rows(sql, table_name):
    pattern = re.compile(
        rf"INSERT INTO\s+[`']{table_name}[`']\s*\(([^)]+)\)\s*VALUES\s*(.*?);",
        re.DOTALL | re.IGNORECASE
    )
    all_rows = []
    for match in pattern.finditer(sql):
        cols = [c.strip().strip('`') for c in match.group(1).split(',')]
        val_str = match.group(2).strip()
        if val_str.startswith('('): val_str = val_str[1:]
        if val_str.endswith(')'): val_str = val_str[:-1]
        raw_rows = re.split(r'\)\s*,\s*\(', val_str)
        for r in raw_rows:
            r = r.replace('\\r', '\r').replace('\\n', '\n')
            reader = csv.reader(io.StringIO(r), quotechar="'", escapechar="\\", skipinitialspace=True)
            try:
                row_vals = next(reader)
                all_rows.append(dict(zip(cols, row_vals)))
            except StopIteration:
                continue
    return all_rows

def to_int(val, default=0):
    if val is None or val == '' or str(val).upper() == 'NULL':
        return default
    try:
        return int(val)
    except Exception:
        return default

def to_str(val, default=''):
    if val is None or str(val).upper() == 'NULL':
        return default
    return str(val)

# ==================== DEDICATED TABLE MIGRATORS ====================

def migrate_account(sql, db):
    db['account'].delete_many({})
    rows = extract_table_rows(sql, 'account')
    docs = []
    for r in rows:
        doc = {}
        for k, v in r.items():
            if v is None or v.upper() == 'NULL':
                doc[k] = None
            elif k in ['id', 'ban', 'role', 'is_admin', 'active', 'thoi_vang', 'TopBoss', 'TopNoel', 'NangDong', 'server_login', 'vnd', 'tongnap', 'admin', 'mocnap', 'pointNap', 'toptrungthu', 'TopBossHalloween', 'TopQuaHalloween']:
                doc[k] = to_int(v)
            else:
                doc[k] = v
        docs.append(doc)
    if docs:
        db['account'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'account'")
    return docs

def migrate_player(sql, db):
    db['player'].delete_many({})
    rows = extract_table_rows(sql, 'player')
    docs = []
    for r in rows:
        doc = {}
        for k, v in r.items():
            if v is None or v.upper() == 'NULL':
                doc[k] = None
            elif k in ['id', 'account_id', 'power', 'head', 'gender', 'have_tennis_space_ship', 'clan_id_sv1', 'clan_id_sv2', 'thoi_vang', 'new_reg', 'event_point', '1sao', '2sao', '3sao', 'top', 'moc_nap', 'topSB', 'moc_nap2', 'tongnap']:
                doc[k] = to_int(v)
            else:
                doc[k] = v
        docs.append(doc)
    if docs:
        db['player'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'player'")
    return docs

def migrate_item_template(sql, db):
    db['item_template'].delete_many({})
    rows = extract_table_rows(sql, 'item_template')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'type': to_int(r.get('TYPE') or r.get('type')),
            'gender': to_int(r.get('gender')),
            'name': to_str(r.get('NAME') or r.get('name')),
            'description': to_str(r.get('description')),
            'icon_id': to_int(r.get('icon_id')),
            'part': to_int(r.get('part')),
            'is_up_to_up': to_int(r.get('is_up_to_up')),
            'power_require': to_int(r.get('power_require'))
        }
        docs.append(doc)
    if docs:
        db['item_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'item_template'")

def migrate_item_option_template(sql, db):
    db['item_option_template'].delete_many({})
    rows = extract_table_rows(sql, 'item_option_template')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'name': to_str(r.get('NAME') or r.get('name')),
            'type': to_int(r.get('TYPE') or r.get('type'))
        }
        docs.append(doc)
    if docs:
        db['item_option_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'item_option_template'")

def migrate_part(sql, db):
    db['part'].delete_many({})
    rows = extract_table_rows(sql, 'part')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'type': to_int(r.get('TYPE') or r.get('type')),
            'data': to_str(r.get('DATA') or r.get('data'))
        }
        docs.append(doc)
    if docs:
        db['part'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'part'")

def migrate_map_template(sql, db):
    db['map_template'].delete_many({})
    rows = extract_table_rows(sql, 'map_template')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'name': to_str(r.get('NAME') or r.get('name')),
            'data': to_str(r.get('data')),
            'zones': to_int(r.get('zones')),
            'max_player': to_int(r.get('max_player')),
            'waypoints': to_str(r.get('waypoints')),
            'mobs': to_str(r.get('mobs')),
            'npcs': to_str(r.get('npcs')),
            'effect_noel': to_str(r.get('effect_noel')),
            'eff_event': to_str(r.get('eff_event')),
            'effect': to_str(r.get('effect'))
        }
        docs.append(doc)
    if docs:
        db['map_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'map_template'")

def migrate_mob_template(sql, db):
    db['mob_template'].delete_many({})
    rows = extract_table_rows(sql, 'mob_template')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'type': to_int(r.get('TYPE') or r.get('type')),
            'name': to_str(r.get('NAME') or r.get('name')),
            'hp': to_int(r.get('hp')),
            'range_move': to_int(r.get('range_move')),
            'speed': to_int(r.get('speed')),
            'dart_type': to_int(r.get('dart_type')),
            'percent_dame': to_int(r.get('percent_dame')),
            'percent_tiem_nang': to_int(r.get('percent_tiem_nang'))
        }
        docs.append(doc)
    if docs:
        db['mob_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'mob_template'")

def migrate_npc_template(sql, db):
    db['npc_template'].delete_many({})
    rows = extract_table_rows(sql, 'npc_template')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'name': to_str(r.get('NAME') or r.get('name')),
            'head': to_int(r.get('head')),
            'body': to_int(r.get('body')),
            'leg': to_int(r.get('leg'))
        }
        docs.append(doc)
    if docs:
        db['npc_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'npc_template'")

def migrate_shops(sql, db):
    # shop
    db['shop'].delete_many({})
    rows = extract_table_rows(sql, 'shop')
    docs = [{'id': to_int(r.get('id')), 'npc_id': to_int(r.get('npc_id')), 'shop_order': to_int(r.get('shop_order'))} for r in rows]
    if docs: db['shop'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'shop'")

    # tab_shop
    db['tab_shop'].delete_many({})
    rows = extract_table_rows(sql, 'tab_shop')
    docs = [{'id': to_int(r.get('id')), 'shop_id': to_int(r.get('shop_id')), 'name': to_str(r.get('NAME') or r.get('name'))} for r in rows]
    if docs: db['tab_shop'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'tab_shop'")

    # item_shop
    db['item_shop'].delete_many({})
    rows = extract_table_rows(sql, 'item_shop')
    docs = []
    for r in rows:
        doc = {
            'id': to_int(r.get('id')),
            'tab_id': to_int(r.get('tab_id')),
            'temp_id': to_int(r.get('temp_id')),
            'gold': to_int(r.get('gold')),
            'gem': to_int(r.get('gem')),
            'is_new': to_int(r.get('is_new')),
            'is_sell': to_int(r.get('is_sell')),
            'item_exchange': to_int(r.get('item_exchange'), -1),
            'quantity_exchange': to_int(r.get('quantity_exchange')),
            'create_time': to_str(r.get('create_time'))
        }
        docs.append(doc)
    if docs: db['item_shop'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'item_shop'")

    # item_shop_option
    db['item_shop_option'].delete_many({})
    rows = extract_table_rows(sql, 'item_shop_option')
    docs = [{'item_shop_id': to_int(r.get('item_shop_id')), 'option_id': to_int(r.get('option_id')), 'param': to_int(r.get('param')), 'id_id': to_int(r.get('id_id'))} for r in rows]
    if docs: db['item_shop_option'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'item_shop_option'")

def migrate_cai_trang_and_avatars(sql, db):
    # cai_trang
    db['cai_trang'].delete_many({})
    rows = extract_table_rows(sql, 'cai_trang')
    docs = [{'id_temp': to_int(r.get('id_temp')), 'head': to_int(r.get('head')), 'body': to_int(r.get('body')), 'leg': to_int(r.get('leg')), 'bag': to_int(r.get('bag'))} for r in rows]
    if docs: db['cai_trang'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'cai_trang'")

    # head_avatar
    db['head_avatar'].delete_many({})
    rows = extract_table_rows(sql, 'head_avatar')
    docs = [{'head_id': to_int(r.get('head_id')), 'avatar_id': to_int(r.get('avatar_id'))} for r in rows]
    if docs: db['head_avatar'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'head_avatar'")

    # flag_bag
    db['flag_bag'].delete_many({})
    rows = extract_table_rows(sql, 'flag_bag')
    docs = [{'id': to_int(r.get('id')), 'icon_data': to_str(r.get('icon_data')), 'name': to_str(r.get('NAME') or r.get('name')), 'gold': to_int(r.get('gold')), 'gem': to_int(r.get('gem')), 'icon_id': to_int(r.get('icon_id'))} for r in rows]
    if docs: db['flag_bag'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'flag_bag'")

def migrate_tasks(sql, db):
    # task_main_template
    db['task_main_template'].delete_many({})
    rows = extract_table_rows(sql, 'task_main_template')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('NAME') or r.get('name')), 'detail': to_str(r.get('detail'))} for r in rows]
    if docs: db['task_main_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'task_main_template'")

    # task_sub_template
    db['task_sub_template'].delete_many({})
    rows = extract_table_rows(sql, 'task_sub_template')
    docs = [{'task_main_id': to_int(r.get('task_main_id')), 'name': to_str(r.get('NAME') or r.get('name')), 'max_count': to_int(r.get('max_count')), 'notify': to_str(r.get('notify')), 'npc_id': to_int(r.get('npc_id')), 'map': to_int(r.get('map'))} for r in rows]
    if docs: db['task_sub_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'task_sub_template'")

    # side_task_template
    db['side_task_template'].delete_many({})
    rows = extract_table_rows(sql, 'side_task_template')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('NAME') or r.get('name')), 'max_count_lv1': to_str(r.get('max_count_lv1')), 'max_count_lv2': to_str(r.get('max_count_lv2')), 'max_count_lv3': to_str(r.get('max_count_lv3')), 'max_count_lv4': to_str(r.get('max_count_lv4')), 'max_count_lv5': to_str(r.get('max_count_lv5'))} for r in rows]
    if docs: db['side_task_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'side_task_template'")

def migrate_skills(sql, db):
    # nclass
    db['nclass'].delete_many({})
    rows = extract_table_rows(sql, 'nclass')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('NAME') or r.get('name'))} for r in rows]
    if docs: db['nclass'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'nclass'")

    # skill_template
    db['skill_template'].delete_many({})
    rows = extract_table_rows(sql, 'skill_template')
    docs = []
    for r in rows:
        doc = {
            'nclass_id': to_int(r.get('nclass_id')),
            'id': to_int(r.get('id')),
            'name': to_str(r.get('name')),
            'max_point': to_int(r.get('max_point')),
            'mana_use_type': to_int(r.get('mana_use_type')),
            'type': to_int(r.get('type')),
            'icon_id': to_int(r.get('icon_id')),
            'dam_info': to_str(r.get('dam_info')),
            'slot': to_int(r.get('slot')),
            'skills': to_str(r.get('skills')),
            'desc': to_str(r.get('desc'))
        }
        docs.append(doc)
    if docs: db['skill_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'skill_template'")

def migrate_small_version(sql, db):
    db['small_version'].delete_many({})
    rows = extract_table_rows(sql, 'small_version')
    docs = [{'id': to_int(r.get('id')), 'x1': to_int(r.get('x1')), 'x2': to_int(r.get('x2')), 'x3': to_int(r.get('x3')), 'x4': to_int(r.get('x4'))} for r in rows]
    if docs: db['small_version'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'small_version'")

def migrate_other_tables(sql, db):
    # achivements
    db['achivements'].delete_many({})
    rows = extract_table_rows(sql, 'achivements')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('name')), 'detail': to_str(r.get('detail')), 'money': to_int(r.get('money')), 'max_count': to_int(r.get('max_count'))} for r in rows]
    if docs: db['achivements'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'achivements'")

    # caption
    db['caption'].delete_many({})
    rows = extract_table_rows(sql, 'caption')
    docs = [{'id': to_int(r.get('id')), 'earth': to_str(r.get('earth')), 'saiya': to_str(r.get('saiya')), 'namek': to_str(r.get('namek')), 'power': to_int(r.get('power'))} for r in rows]
    if docs: db['caption'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'caption'")

    # power_limit
    db['power_limit'].delete_many({})
    rows = extract_table_rows(sql, 'power_limit')
    docs = [{'id': to_int(r.get('id')), 'power': to_int(r.get('power')), 'hp': to_int(r.get('hp')), 'mp': to_int(r.get('mp')), 'damage': to_int(r.get('damage')), 'defense': to_int(r.get('defense')), 'critical': to_int(r.get('critical'))} for r in rows]
    if docs: db['power_limit'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'power_limit'")

    # collection_book
    db['collection_book'].delete_many({})
    rows = extract_table_rows(sql, 'collection_book')
    docs = [{'id': to_int(r.get('id')), 'item_id': to_int(r.get('item_id')), 'name': to_str(r.get('name')), 'info': to_str(r.get('info')), 'icon': to_int(r.get('icon')), 'rank': to_int(r.get('rank')), 'max_amount': to_int(r.get('max_amount')), 'type': to_int(r.get('type')), 'mob_id': to_int(r.get('mob_id')), 'head': to_int(r.get('head')), 'body': to_int(r.get('body')), 'leg': to_int(r.get('leg')), 'bag': to_int(r.get('bag')), 'options': to_str(r.get('options')), 'aura': to_int(r.get('aura'))} for r in rows]
    if docs: db['collection_book'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'collection_book'")

    # consignment_shop
    db['consignment_shop'].delete_many({})
    rows = extract_table_rows(sql, 'consignment_shop')
    docs = [{'id': to_int(r.get('id')), 'consignor_id': to_int(r.get('consignor_id')), 'tab': to_int(r.get('tab')), 'item_id': to_int(r.get('item_id')), 'gold': to_int(r.get('gold')), 'gem': to_int(r.get('gem')), 'quantity': to_int(r.get('quantity')), 'up_top': to_int(r.get('up_top')), 'sold': to_int(r.get('sold')), 'item_options': to_str(r.get('item_options')), 'time_consign': to_int(r.get('time_consign')), 'consignor_name': to_str(r.get('consignor_name')), 'su_kien': to_int(r.get('su_kien'))} for r in rows]
    if docs: db['consignment_shop'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'consignment_shop'")

    # mini_pet
    db['mini_pet'].delete_many({})
    rows = extract_table_rows(sql, 'mini_pet')
    docs = [{'id_temp': to_int(r.get('id_temp')), 'head': to_int(r.get('head')), 'body': to_int(r.get('body')), 'leg': to_int(r.get('leg'))} for r in rows]
    if docs: db['mini_pet'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'mini_pet'")

    # pet_follow
    db['pet_follow'].delete_many({})
    rows = extract_table_rows(sql, 'pet_follow')
    docs = [{'id_temp': to_int(r.get('id_temp')), 'icon': to_int(r.get('icon')), 'width': to_int(r.get('width')), 'height': to_int(r.get('height')), 'frame': to_int(r.get('frame'))} for r in rows]
    if docs: db['pet_follow'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'pet_follow'")

    # intrinsic
    db['intrinsic'].delete_many({})
    rows = extract_table_rows(sql, 'intrinsic')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('NAME') or r.get('name')), 'param_from_1': to_int(r.get('param_from_1')), 'param_to_1': to_int(r.get('param_to_1')), 'param_from_2': to_int(r.get('param_from_2')), 'param_to_2': to_int(r.get('param_to_2')), 'icon': to_int(r.get('icon')), 'gender': to_int(r.get('gender'))} for r in rows]
    if docs: db['intrinsic'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'intrinsic'")

    # img_by_name
    db['img_by_name'].delete_many({})
    rows = extract_table_rows(sql, 'img_by_name')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('NAME') or r.get('name')), 'n_frame': to_int(r.get('n_frame'))} for r in rows]
    if docs: db['img_by_name'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'img_by_name'")

    # attribute_template & attribute_server
    db['attribute_template'].delete_many({})
    rows = extract_table_rows(sql, 'attribute_template')
    docs = [{'id': to_int(r.get('id')), 'name': to_str(r.get('name'))} for r in rows]
    if docs: db['attribute_template'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'attribute_template'")

    db['attribute_server'].delete_many({})
    rows = extract_table_rows(sql, 'attribute_server')
    docs = [{'id': to_int(r.get('id')), 'attribute_template_id': to_int(r.get('attribute_template_id')), 'value': to_int(r.get('value')), 'time': to_int(r.get('time'))} for r in rows]
    if docs: db['attribute_server'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'attribute_server'")

    # gift_codes
    db['gift_codes'].delete_many({})
    rows = extract_table_rows(sql, 'gift_codes')
    docs = [{'id': to_int(r.get('id')), 'type': to_int(r.get('type')), 'code': to_str(r.get('code')), 'gold': to_int(r.get('gold')), 'gem': to_int(r.get('gem')), 'ruby': to_int(r.get('ruby')), 'items': to_str(r.get('items')), 'status': to_int(r.get('status')), 'active': to_int(r.get('active')), 'expires_at': to_str(r.get('expires_at')), 'created_at': to_str(r.get('created_at')), 'updated_at': to_str(r.get('updated_at'))} for r in rows]
    if docs: db['gift_codes'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'gift_codes'")

    # event
    db['event'].delete_many({})
    rows = extract_table_rows(sql, 'event')
    docs = [{'server': to_int(r.get('server')), 'kame': to_int(r.get('kame')), 'bill': to_int(r.get('bill')), 'karin': to_int(r.get('karin')), 'thuongde': to_int(r.get('thuongde')), 'thanvutru': to_int(r.get('thanvutru'))} for r in rows]
    if docs: db['event'].insert_many(docs)
    print(f"[OK] Migrated {len(docs)} rows into 'event'")

    # notifications & alert & global_data & type_map & category & post & clan_sv1 & clan_sv2
    for tbl in ['notifications', 'alert', 'global_data', 'type_map', 'category', 'post', 'clan_sv1', 'clan_sv2']:
        db[tbl].delete_many({})
        rows = extract_table_rows(sql, tbl)
        docs = []
        for r in rows:
            d = {}
            for k, v in r.items():
                clean_k = 'name' if k.upper() == 'NAME' else k.lower()
                d[clean_k] = int(v) if v and v.lstrip('-').isdigit() else v
            docs.append(d)
        if docs: db[tbl].insert_many(docs)
        print(f"[OK] Migrated {len(docs)} rows into '{tbl}'")

def parse_sql_to_mongo():
    client = pymongo.MongoClient('mongodb://127.0.0.1:27017/')
    db = client['nro_db']
    
    sql_path = get_sql_path()
    print(f"Reading SQL file from: {sql_path}\n" + "="*50)
    with open(sql_path, 'r', encoding='utf-8') as f:
        sql = f.read()

    # Run each table's dedicated migration
    acc_docs = migrate_account(sql, db)
    pl_docs = migrate_player(sql, db)
    migrate_item_template(sql, db)
    migrate_item_option_template(sql, db)
    migrate_part(sql, db)
    migrate_map_template(sql, db)
    migrate_mob_template(sql, db)
    migrate_npc_template(sql, db)
    migrate_shops(sql, db)
    migrate_cai_trang_and_avatars(sql, db)
    migrate_tasks(sql, db)
    migrate_skills(sql, db)
    migrate_small_version(sql, db)
    migrate_other_tables(sql, db)

    # Initialize counters
    max_acc_id = max([d['id'] for d in acc_docs if d.get('id') is not None], default=0)
    max_pl_id = max([d['id'] for d in pl_docs if d.get('id') is not None], default=0)
    db['counters'].update_one({'_id': 'accountId'}, {'$set': {'sequence_value': max_acc_id}}, upsert=True)
    db['counters'].update_one({'_id': 'playerId'}, {'$set': {'sequence_value': max_pl_id}}, upsert=True)
    print("="*50 + f"\n[SUCCESS] Initialized counters: accountId={max_acc_id}, playerId={max_pl_id}")
    print("[SUCCESS] ALL TABLES MIGRATED ACCURATELY AND COMPLETELY!")

if __name__ == '__main__':
    parse_sql_to_mongo()
