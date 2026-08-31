import re

with open('Manager.java', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('PreparedStatement ps = null;\n        ResultSet rs = null;\n        try (Connection con = DBService.gI().getConnectionForGame();) {', 'try {\n            com.mongodb.client.MongoDatabase db = nro.jdbc.MongoDBConnection.getDatabase();')

content = content.replace('ps = con.prepareStatement(\"select count(id) from map_template\", ResultSet.TYPE_SCROLL_INSENSITIVE,\n                    ResultSet.CONCUR_READ_ONLY);\n            rs = ps.executeQuery();\n            if (rs.first()) {\n                int countRow = rs.getShort(1);\n                MAP_TEMPLATES = new MapTemplate[countRow];', 'long countRow = db.getCollection(\"map_template\").countDocuments();\n            if (countRow > 0) {\n                MAP_TEMPLATES = new MapTemplate[(int)countRow];')

content = content.replace('ps = con.prepareStatement(\"select * from map_template\");\n                rs = ps.executeQuery();', 'com.mongodb.client.MongoCursor<org.bson.Document> rs = db.getCollection(\"map_template\").find().iterator();')

with open('Manager.java', 'w', encoding='utf-8') as f:
    f.write(content)
