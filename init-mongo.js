db = db.getSiblingDB("lottery_system_db");

db.createUser({
  user: "lottery_user",
  pwd: "lottery_password",
  roles: [{ role: "readWrite", db: "lottery_system_db" }]
});

// Initialize empty collections for lottery system
db.participants.createIndex({ "id": 1 }, { unique: true });
db.participants.createIndex({ "email": 1 }, { unique: true });