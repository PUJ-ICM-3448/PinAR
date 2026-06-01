const {onDocumentCreated, onDocumentUpdated} = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

exports.notifyCommunityMembersOnPinShare = onDocumentUpdated(
  "cloud_anchor_pins/{pinId}",
  async (event) => {
    const before = event.data.before.data() || {};
    const after = event.data.after.data() || {};
    const addedCommunityIds = getAddedValues(before.comunidades, after.comunidades);

    if (addedCommunityIds.length === 0) {
      logger.info("Esta actualizacion no agrega comunidades al pin, skippeando.", {
        pinId: event.params.pinId,
      });
      return;
    }

    await notifyAddedCommunities(event.params.pinId, after, addedCommunityIds);
  },
);

exports.notifyCommunityMembersOnPinCreate = onDocumentCreated(
  "cloud_anchor_pins/{pinId}",
  async (event) => {
    const pin = event.data.data() || {};
    const communityIds = toStringArray(pin.comunidades);

    if (communityIds.length === 0) {
      logger.info("Este nuevo pin no tiene comunidades asociadas aun, skippeando.", {
        pinId: event.params.pinId,
      });
      return;
    }

    await notifyAddedCommunities(event.params.pinId, pin, communityIds);
  },
);

async function notifyAddedCommunities(pinId, pin, communityIds) {
  const createdBy = typeof pin.createdBy === "string" ? pin.createdBy : "";

  for (const communityId of communityIds) {
    const communitySnap = await db.collection("comunidades").doc(communityId).get();

    if (!communitySnap.exists) {
      logger.warn("No se ha encontrado la comunidad a la que se le quiere compartir el pin.", {
        pinId,
        communityId,
      });
      continue;
    }

    const community = communitySnap.data() || {};
    const memberIds = toStringArray(community.members).filter((uid) => uid !== createdBy);

    if (memberIds.length === 0) {
      logger.info("Comunidad sin miembros.", {pinId, communityId});
      continue;
    }

    const tokens = await getMemberTokens(memberIds);

    if (tokens.length === 0) {
      logger.info("Los miembros de la comunidad no tienen tokens de FCM.", {pinId, communityId});
      continue;
    }

    const title = community.name
      ? `Nuevo pin en ${community.name}`
      : "Nuevo pin en tu comunidad";
    const body = pin.title
      ? `Se compartio "${pin.title}" contigo.`
      : "Alguien compartio un nuevo pin contigo.";

    await sendTokenBatches(tokens, {
      notification: {title, body},
      data: {
        type: "community_pin_shared",
        pinId,
        communityId,
        communityName: stringOrEmpty(community.name),
        pinTitle: stringOrEmpty(pin.title),
      },
      android: {
        priority: "high",
      },
    });

    logger.info("Notificacion enviada a la comunidad.", {
      pinId,
      communityId,
      recipients: tokens.length,
    });
  }
}

async function getMemberTokens(memberIds) {
  const userRefs = memberIds.map((uid) => db.collection("usuarios").doc(uid));
  const userSnaps = await db.getAll(...userRefs);
  const tokens = new Set();

  for (const snap of userSnaps) {
    if (!snap.exists) {
      continue;
    }

    const user = snap.data() || {};
    const token = user.FCMToken || user.fcmToken;
    if (typeof token === "string" && token.trim() !== "") {
      tokens.add(token);
    }
  }

  return [...tokens];
}

async function sendTokenBatches(tokens, messageBase) {
  const batchSize = 500;

  for (let start = 0; start < tokens.length; start += batchSize) {
    const batch = tokens.slice(start, start + batchSize);
    const messages = batch.map((token) => ({
      token,
      ...messageBase,
    }));

    const response = await messaging.sendEach(messages);
    if (response.failureCount > 0) {
      logger.warn("Algunos mensajes de FCM fallaron.", {
        failureCount: response.failureCount,
        successCount: response.successCount,
      });
    }
  }
}

function getAddedValues(beforeValue, afterValue) {
  const before = new Set(toStringArray(beforeValue));
  return toStringArray(afterValue).filter((value) => !before.has(value));
}

function toStringArray(value) {
  if (!Array.isArray(value)) {
    return [];
  }

  return value.filter((item) => typeof item === "string" && item.trim() !== "");
}

function stringOrEmpty(value) {
  return typeof value === "string" ? value : "";
}
