import { completeSession, CompleteSessionRequest } from '../typing/api';

export interface QueuedSession {
  sessionId: string;
  request: CompleteSessionRequest;
  timestamp: number;
}

const DB_NAME = 'devtype_offline_db';
const STORE_NAME = 'pending_sessions';
const DB_VERSION = 1;

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    if (typeof indexedDB === 'undefined') {
      reject(new Error('IndexedDB not supported'));
      return;
    }
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'sessionId' });
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

export async function enqueueOfflineSession(sessionId: string, request: CompleteSessionRequest): Promise<void> {
  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const item: QueuedSession = {
      sessionId,
      request,
      timestamp: Date.now(),
    };
    store.put(item);
  } catch (err) {
    console.warn('Failed to enqueue offline session in IndexedDB:', err);
  }
}

export async function flushOfflineQueue(): Promise<number> {
  if (typeof navigator !== 'undefined' && !navigator.onLine) {
    return 0;
  }

  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);

    const getAllReq = store.getAll();
    return new Promise((resolve) => {
      getAllReq.onsuccess = async () => {
        const items = getAllReq.result as QueuedSession[];
        if (!items || items.length === 0) {
          resolve(0);
          return;
        }

        let flushedCount = 0;
        for (const item of items) {
          try {
            await completeSession(item.sessionId, item.request);
            // Muvaffaqiyatli ketgach o'chiramiz
            const deleteTx = db.transaction(STORE_NAME, 'readwrite');
            deleteTx.objectStore(STORE_NAME).delete(item.sessionId);
            flushedCount++;
          } catch (e) {
            console.warn(`Failed to flush session ${item.sessionId}:`, e);
          }
        }
        resolve(flushedCount);
      };
      getAllReq.onerror = () => resolve(0);
    });
  } catch {
    return 0;
  }
}

// Online hodisasini tinglash
if (typeof window !== 'undefined') {
  window.addEventListener('online', () => {
    flushOfflineQueue().then((count) => {
      if (count > 0) {
        console.info(`Flushed ${count} offline typing sessions.`);
      }
    });
  });
}
