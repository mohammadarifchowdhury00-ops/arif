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
const ADMIN_UID = "admin_arif";
const ADMIN_EMAIL = "Mohammadarifchowdhury00@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function tsNow() {
  return new Date(Date.now() - 1000);
}

function makeUserProfile(uid, email = "alice@gmail.com") {
  return {
    userId: uid,
    displayName: "Alice Learner",
    email,
    role: "student",
    totalXp: 0,
    dailyXp: 0,
    weeklyXp: 0,
    monthlyXp: 0,
    level: 1,
    streakDays: 1,
    lastStudyDate: "2026-11-15",
    dailyGoalMinutes: 15,
    todayStudyMinutes: 0,
    totalStudyMinutes: 0,
    completedLessonsCount: 0,
    badges: [],
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

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
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read users, leaderboard, announcements, or videos", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("leaderboard").get());
  await assertFails(unauth.collection("announcements").where("published", "==", true).get());
  await assertFails(unauth.collection("learning_videos").where("published", "==", true).get());
  await assertFails(unauth.collection("admin_activity_logs").get());
});

test("Student can create and read own profile, but cannot read Bob's profile or list users", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@gmail.com", email_verified: true }).firestore();

  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set(makeUserProfile(ALICE_UID)));
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(alice.collection("users").get());
});

test("Anti-cheat & Shadow Update: student cannot escalate role, inject ghost field, or jump >600 XP", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set(makeUserProfile(ALICE_UID)));

  await assertFails(alice.collection("users").doc(ALICE_UID).update({ role: "admin", updatedAt: tsNow() }));
  await assertFails(alice.collection("users").doc(ALICE_UID).update({ isVerified: true, updatedAt: tsNow() }));
  await assertFails(alice.collection("users").doc(ALICE_UID).update({ totalXp: 5000, updatedAt: tsNow() }));
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).update({ totalXp: 50, dailyXp: 50, updatedAt: tsNow() }));
});

test("XP transactions are append-only and deduplicated by activityKey", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const txRef = alice.collection("users").doc(ALICE_UID).collection("xp_transactions").doc("starter_1_game");

  await assertSucceeds(txRef.set({
    userId: ALICE_UID,
    activityKey: "starter_1_game",
    sourceType: "game",
    xpAmount: 50,
    description: "IRODORI Starter Lesson 1 Game",
    createdAt: tsNow(),
  }));

  await assertFails(txRef.update({ xpAmount: 100 }));
  await assertFails(txRef.delete());
});

test("Leaderboard allows signed-in reads without PII and blocks cross-user tampering", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@gmail.com", email_verified: true }).firestore();

  await assertSucceeds(alice.collection("leaderboard").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    totalXp: 120,
    dailyXp: 40,
    weeklyXp: 120,
    monthlyXp: 120,
    level: 2,
    streakDays: 3,
    badgeCount: 1,
    createdAt: tsNow(),
    updatedAt: tsNow(),
  }));

  await assertSucceeds(bob.collection("leaderboard").get());
  await assertFails(bob.collection("leaderboard").doc(ALICE_UID).update({ totalXp: 0, updatedAt: tsNow() }));
});

test("Admin security: verified Mohammadarifchowdhury00@gmail.com succeeds, spoofed unverified email & student fail", async () => {
  const student = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const spoofAdmin = testEnv.authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false }).firestore();
  const realAdmin = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true }).firestore();

  const videoPayload = {
    title: "Hiragana Lesson 1",
    descriptionBn: "হিরাগানা পরিচিতি ভিডিও",
    videoUrl: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    bookId: "hiragana",
    lessonNumber: 1,
    published: true,
    featuredOnHome: true,
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };

  await assertFails(student.collection("learning_videos").doc("vid_1").set(videoPayload));
  await assertFails(spoofAdmin.collection("learning_videos").doc("vid_1").set(videoPayload));
  await assertSucceeds(realAdmin.collection("learning_videos").doc("vid_1").set(videoPayload));

  await assertFails(student.collection("admin_activity_logs").get());
  await assertFails(spoofAdmin.collection("users").get());
  await assertSucceeds(realAdmin.collection("users").get());
  await assertSucceeds(realAdmin.collection("admin_activity_logs").doc("log_1").set({
    adminEmail: ADMIN_EMAIL,
    action: "Published video",
    targetType: "learning_video",
    targetId: "vid_1",
    createdAt: tsNow(),
  }));
});
