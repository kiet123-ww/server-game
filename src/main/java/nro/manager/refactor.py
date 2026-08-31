import re

with open('AchiveManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import java.sql.PreparedStatement;', '')
text = text.replace('import java.sql.ResultSet;', '')
text = text.replace('import java.sql.SQLException;', '')

text = text.replace('PreparedStatement ps = DBService.gI().getConnectionForGame().prepareStatement(\"SELECT * FROM chivements\");\n            ResultSet rs = ps.executeQuery();', 
    'com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection(\"achivements\");\n            com.mongodb.client.MongoCursor<org.bson.Document> rs = collection.find().iterator();')

text = re.sub(r'while\s*\(\s*rs\.next\(\)\s*\)\s*\{', r'while (rs.hasNext()) { org.bson.Document doc = rs.next();', text)

text = re.sub(r'rs\.getInt\(\"(.*?)\"\)', r'(doc.getInteger("\1") != null ? doc.getInteger("\1") : 0)', text)
text = re.sub(r'rs\.getString\(\"(.*?)\"\)', r'doc.getString("\1")', text)
text = re.sub(r'rs\.getLong\(\"(.*?)\"\)', r'(doc.getLong("\1") != null ? doc.getLong("\1") : 0L)', text)

text = re.sub(r'rs\.close\(\);', '', text)
text = re.sub(r'ps\.close\(\);', '', text)

with open('AchiveManager.java', 'w', encoding='utf-8') as f:
    f.write(text)
