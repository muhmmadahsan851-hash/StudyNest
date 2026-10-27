const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8085,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write books", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const bookRef = unauthedDb.collection("users").doc("alice").collection("books").doc("book1");

  await assertFails(bookRef.get());
  await assertFails(
    bookRef.set({
      id: "book1",
      userId: "alice",
      title: "Clean Architecture",
      author: "Robert C. Martin",
      category: "Computer Science",
      colorTag: "#4F46E5",
      totalChapters: 10,
      completedChapters: 0,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user can write and read their own book", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const bookRef = aliceDb.collection("users").doc("alice").collection("books").doc("book1");

  await assertSucceeds(
    bookRef.set({
      id: "book1",
      userId: "alice",
      title: "Clean Architecture",
      author: "Robert C. Martin",
      category: "Computer Science",
      colorTag: "#4F46E5",
      totalChapters: 10,
      completedChapters: 0,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(bookRef.get());
});

test("Cross-user access is denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc("alice").collection("books").doc("book1").set({
      id: "book1",
      userId: "alice",
      title: "Clean Architecture",
      author: "Robert C. Martin",
      category: "Computer Science",
      colorTag: "#4F46E5",
      totalChapters: 10,
      completedChapters: 0,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext("bob").firestore();
  const aliceBookForBob = bobDb.collection("users").doc("alice").collection("books").doc("book1");

  await assertFails(aliceBookForBob.get());
  await assertFails(aliceBookForBob.update({ completedChapters: 5 }));
});
