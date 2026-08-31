package nro.jdbc.daos;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import nro.jdbc.MongoDBConnection;
import nro.server.Manager;
import nro.server.io.Session;
import nro.utils.Log;
import nro.utils.Util;

import java.util.Date;
import java.sql.Timestamp;

public class AccountDAO {

    private static MongoCollection<Document> getCollection() {
        return MongoDBConnection.getDatabase().getCollection("account");
    }

    public static void updateAccount(Session session) {
        try {
            getCollection().updateOne(
                    Filters.and(Filters.eq("id", session.userId), Filters.eq("username", session.uu)),
                    Updates.set("password", session.pp)
            );
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        }
    }

    public static void updateLastTimeLoginAllAccount() {
        try {
            getCollection().updateMany(
                    Filters.eq("server_login", Manager.SERVER),
                    Updates.combine(
                            Updates.set("last_time_login", "2000-01-01"),
                            Updates.set("last_time_logout", "2001-01-01")
                    )
            );
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        }
    }

    public static void updateAccoutLogout(Session session) {
        if (session.uu != null && session.pp != null) {
            try {
                getCollection().updateOne(
                        Filters.eq("id", session.userId),
                        Updates.set("last_time_logout", new Timestamp(System.currentTimeMillis()))
                );
            } catch (Exception e) {
                Log.error(AccountDAO.class, e);
            }
        }
    }

    public static void banAccount(Session session) {
        try {
            getCollection().updateOne(
                    Filters.and(Filters.eq("id", session.userId), Filters.eq("username", session.uu)),
                    Updates.set("ban", 1)
            );
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        }
    }

    private static int getNextSequenceValue(String sequenceName) {
        MongoCollection<Document> counters = MongoDBConnection.getDatabase().getCollection("counters");
        Document query = new Document("_id", sequenceName);
        Document update = new Document("$inc", new Document("sequence_value", 1));
        Document result = counters.findOneAndUpdate(query, update);
        if (result == null) {
            counters.insertOne(new Document("_id", sequenceName).append("sequence_value", 1));
            return 1;
        }
        return result.getInteger("sequence_value") + 1;
    }

    public static int createAccount(String user, String password) {
        try {
            Document existing = getCollection().find(Filters.eq("username", user)).first();
            if (existing != null) {
                return -1;
            }

            int newId = getNextSequenceValue("accountId");
            Document newAccount = new Document("id", newId)
                    .append("username", user)
                    .append("password", password)
                    .append("create_time", new Timestamp(System.currentTimeMillis()))
                    .append("update_time", new Timestamp(System.currentTimeMillis()))
                    .append("ban", 0)
                    .append("role", -1)
                    .append("is_admin", 0)
                    .append("server_login", 1)
                    .append("vnd", 1000)
                    .append("tongnap", 1000)
                    .append("admin", 0)
                    .append("coin", 0);

            getCollection().insertOne(newAccount);
            return newId;
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
            return -1;
        }
    }

    public static void updatePass(Session session, String mkmoi) {
        try {
            getCollection().updateOne(
                    Filters.and(Filters.eq("id", session.userId), Filters.eq("username", session.uu)),
                    Updates.set("password", mkmoi)
                );
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        }
    }
}
