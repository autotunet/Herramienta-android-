const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
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

test("Unauthenticated user: cannot read notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("notes").get());
});

test("Unauthenticated user: cannot create notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("notes").doc("note1").set({
    id: "note1",
    userId: "someone",
    title: "Test",
    content: "Content"
  }));
});

test("Authenticated user: cannot read another user's note", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("notes").doc("bob_note").set({
      id: "bob_note",
      title: "Bob Note",
      content: "Bob Secret",
      userId: BOB_UID,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("notes").doc("bob_note").get());
});

test("Authenticated user: can create and read their own note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("notes").doc("alice_note").set({
      id: "alice_note",
      userId: ALICE_UID,
      title: "Alice Note",
      content: "Alice Content",
      category: "Work",
      isPinned: false
    })
  );

  await assertSucceeds(
    aliceDb.collection("notes").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: can create and read their own task", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("tasks").doc("alice_task").set({
      id: "alice_task",
      userId: ALICE_UID,
      title: "Do workout",
      isCompleted: false,
      priority: "HIGH"
    })
  );

  await assertSucceeds(
    aliceDb.collection("tasks").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: cannot spoof userId on note creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("notes").doc("fake_note").set({
      id: "fake_note",
      userId: BOB_UID,
      title: "Spoofed Note",
      content: "Spoofed Content"
    })
  );
});
