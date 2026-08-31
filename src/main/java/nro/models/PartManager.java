/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models;

import com.google.gson.Gson;
import java.io.IOException;
import nro.server.io.Message;
import nro.utils.Log;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 *
 * @Build by bkt
 */
public class PartManager {

    private static final PartManager instance = new PartManager();

    public static PartManager getInstance() {
        return instance;
    }

    @Getter
    private List<Part> parts;
    @Getter
    private byte[] data;

    public PartManager() {
        parts = new ArrayList<>();
    }

    public void load() {
        try {
            Gson gson = new Gson();
            com.mongodb.client.MongoCollection<org.bson.Document> collection = nro.jdbc.MongoDBConnection.getDatabase().getCollection("part");
            try (com.mongodb.client.MongoCursor<org.bson.Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    org.bson.Document doc = cursor.next();
                    short id = doc.getInteger("id").shortValue();
                    byte type = doc.getInteger("TYPE").byteValue();
                    String partJson = doc.getString("DATA");
                    int[][] partData = gson.fromJson(partJson, int[][].class);
                    Part part = new Part();
                    part.setId(id);
                    part.setType(type);
                    part.setPartData(partData);
                    add(part);
                }
            }
            Log.success("Load part thành công (" + parts.size() + ")");
            setData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(Part part) {
        parts.add(part);
    }

    public void remove(Part part) {
        parts.remove(part);
    }

    public Part find(int id) {
        for (Part part : parts) {
            if (part.getId() == id) {
                return part;
            }
        }
        return null;
    }

    public Part get(int index) {
        return parts.get(index);
    }

    public void setData() {
        try {
            Message ms = new Message();
            DataOutputStream ds = ms.writer();
            ds.writeShort(parts.size());
            for (Part part : parts) {
                ds.writeByte(part.getType());
                int[][] partData = part.getPartData();
                for (int[] rowData : partData) {
                    ds.writeShort(rowData[0]);
                    ds.writeByte(rowData[1]);
                    ds.writeByte(rowData[2]);
                }
            }
            ds.flush();
            this.data = ms.getData();
            ds.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
