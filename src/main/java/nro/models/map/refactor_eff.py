import re

with open('EffectEventManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import java.sql.PreparedStatement;', '')
text = text.replace('import java.sql.ResultSet;', '')
text = text.replace('import java.sql.SQLException;', '')

text = text.replace('PreparedStatement ps = DBService.gI().getConnectionForGame()\n                    .prepareStatement(\"select * from effect_event\");\n            ResultSet rs = ps.executeQuery();', 
    'com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection(\"effect_event\");\n            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();')

text = re.sub(r'while\s*\(\s*rs\.next\(\)\s*\)\s*\{', r'while (rs.hasNext()) { org.bson.Document doc = rs.next();', text)

text = re.sub(r'rs\.getInt\(\"(.*?)\"\)', r'(doc.getInteger("\1") != null ? doc.getInteger("\1") : 0)', text)
text = re.sub(r'rs\.getString\(\"(.*?)\"\)', r'doc.getString("\1")', text)
text = re.sub(r'rs\.getByte\(\"(.*?)\"\)', r'(doc.getInteger("\1") != null ? doc.getInteger("\1").byteValue() : 0)', text)

text = re.sub(r'rs\.close\(\);', '', text)
text = re.sub(r'ps\.close\(\);', '', text)

with open('EffectEventManager.java', 'w', encoding='utf-8') as f:
    f.write(text)
