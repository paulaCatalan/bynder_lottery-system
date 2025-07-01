db = db.getSiblingDB("mydatabase");

db.createUser({
  user: "myuser",
  pwd: "mypassword",
  roles: [{ role: "readWrite", db: "mydatabase" }]
});

db.sampleCollection.insertMany([
  { name: "Alice", age: 30 },
  { name: "Bob", age: 25 }
]);