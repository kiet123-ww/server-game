import re

with open('Manager.java', 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Replace the ps = con.prepareStatement block with rs = db.getCollection(...).find().iterator()
# We will use a regex to capture the table name.
def repl_query(match):
    table = match.group(1)
    return f"com.mongodb.client.MongoCursor<org.bson.Document> rs_{table} = db.getCollection(\"{table}\").find().iterator();"

# We have to be careful. Let's just do a generic replacement for the queries.
text = re.sub(r'ps = con\.prepareStatement\(\"select \* from ([a-zA-Z0-9_]+)[^\"]*\"\);\s*rs = ps\.executeQuery\(\);', 
              r'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection("\1").find().iterator();', text)

# There are some specific queries:
text = text.replace('ps = con.prepareStatement(\"SELECT id, task_main_template.name, detail, \"\n                    + \"text_of_help, text_of_confirm, npc_id, map_tasks, text_of_step, text_of_step_2 from task_main_template\");\n            rs = ps.executeQuery();', 'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection(\"task_main_template\").find().iterator();')
text = text.replace('ps = con.prepareStatement(\"select id, name from item_option_template\");\n            rs = ps.executeQuery();', 'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection(\"item_option_template\").find().iterator();')
text = text.replace('ps = con.prepareStatement(\"select name, n_frame from img_by_name\");\n            rs = ps.executeQuery();', 'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection(\"img_by_name\").find().iterator();')
text = text.replace('ps = con.prepareStatement(\"select id from clan_sv\" + SERVER + \" order by id desc limit 1\");\n            rs = ps.executeQuery();', 'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection(\"clan_sv\" + SERVER).find().sort(com.mongodb.client.model.Sorts.descending(\"id\")).limit(1).iterator();')


# 2. Replace while (rs.next()) with while (rs.hasNext()) { Document doc = rs.next();
text = re.sub(r'while\s*\(\s*rs\.next\(\)\s*\)\s*\{', r'while (rs.hasNext()) { org.bson.Document doc = rs.next();', text)

# 3. Replace rs.get with doc.get
text = re.sub(r'rs\.getInt\((.*?)\)', r'(doc.getInteger(\1) != null ? doc.getInteger(\1) : 0)', text)
text = re.sub(r'rs\.getByte\((.*?)\)', r'(doc.getInteger(\1) != null ? doc.getInteger(\1).byteValue() : 0)', text)
text = re.sub(r'rs\.getShort\((.*?)\)', r'(doc.getInteger(\1) != null ? doc.getInteger(\1).shortValue() : 0)', text)
text = re.sub(r'rs\.getString\((.*?)\)', r'doc.getString(\1)', text)
text = re.sub(r'rs\.getLong\((.*?)\)', r'(doc.getLong(\1) != null ? doc.getLong(\1) : 0L)', text)
text = re.sub(r'rs\.getBoolean\((.*?)\)', r'(doc.getBoolean(\1) != null ? doc.getBoolean(\1) : false)', text)

# For rs.getTimestamp, MongoDB driver returns Date.
text = re.sub(r'rs\.getTimestamp\((.*?)\)', r'new java.sql.Timestamp(doc.getDate(\1).getTime())', text)

# Remove rs.close() and ps.close()
text = re.sub(r'rs\.close\(\);', '', text)
text = re.sub(r'ps\.close\(\);', '', text)

with open('Manager.java', 'w', encoding='utf-8') as f:
    f.write(text)
