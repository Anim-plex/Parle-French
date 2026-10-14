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

test("Unauthenticated user cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Alice can create and read her own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(
    aliceRef.set({
      userId: ALICE_UID,
      displayName: "Alice Dupont",
      email: "alice@example.com",
      targetLevel: "B1",
      streakDays: 3,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(aliceRef.get());
});

test("Bob cannot read Alice's user profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      targetLevel: "B1",
      streakDays: 1,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Alice can add and query her own flashcards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardRef = aliceDb.collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1");
  await assertSucceeds(
    cardRef.set({
      cardId: "card_1",
      userId: ALICE_UID,
      frenchWord: "bonjour",
      englishMeaning: "hello",
      category: "Daily",
      repetitionCount: 0,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("flashcards").get()
  );
});

test("Bob cannot access Alice's flashcards", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1").set({
      cardId: "card_1",
      userId: ALICE_UID,
      frenchWord: "croissant",
      englishMeaning: "croissant",
      repetitionCount: 1,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("flashcards").doc("card_1").get()
  );
});
