import { writeSnippet, fitCodeToRange } from '../generate-curriculum-800';

export function generateJavascriptCurriculum(): void {
  const topics = [
    'variables',
    'operators',
    'conditions',
    'loops',
    'functions',
    'arrays',
    'objects',
    'strings-and-template-literals',
  ];

  for (const topic of topics) {
    for (const diff of ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as const) {
      for (let i = 1; i <= 5; i++) {
        const snippet = createJavascriptSnippet(topic, diff, i);
        writeSnippet(snippet);
      }
    }
  }
}

function toPascalCase(str: string): string {
  return str.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('');
}

function createJavascriptSnippet(
  topic: string,
  diff: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT',
  index: number
) {
  let title = '';
  let noteEn = '';
  let noteUzl = '';
  let noteRu = '';
  let code = '';
  let type: 'SNIPPET' | 'SPECIAL_CHARS' | 'CHALLENGE' = 'SNIPPET';
  const isRanked = diff === 'INTERMEDIATE' && index <= 4;
  const pascalTopic = toPascalCase(topic);

  if (['variables', 'operators', 'conditions', 'loops'].includes(topic) && diff === 'BEGINNER') {
    type = 'SPECIAL_CHARS';
  }

  if (diff === 'BEGINNER') {
    title = `JavaScript ${topic} Basic Drill ${index}`;
    noteEn = `Beginner drill for JavaScript ${topic}.`;
    noteUzl = `JavaScript ${topic} boʻyicha boshlangʻich darajadagi amaliy mashq.`;
    noteRu = `Базовое упражнение по JavaScript ${topic}.`;

    if (topic === 'variables') {
      const timeouts = [2500, 5000, 7500, 10000, 12500];
      code = `const maxRetries = ${index + 2};\nconst timeoutMs = ${timeouts[index - 1]};\nlet isConnected = true;\nlet sessionToken = "tok_prod_409${index}";`;
    } else if (topic === 'operators') {
      code = `const baseScore = 250;\nconst bonus = 25 * ${index};\nconst isQualified = (baseScore + bonus >= 200) && (bonus < 300);\nconst ratio = baseScore / 10;`;
    } else if (topic === 'conditions') {
      const responses = [75, 88, 105, 125, 150];
      code = `const responseTime = ${responses[index - 1]};\nif (responseTime < 100) {\n  console.log("Status: OPTIMAL");\n} else {\n  console.log("Status: SLOW");\n}`;
    } else if (topic === 'loops') {
      code = `const sequence = [];\nfor (let i = 1; i <= 5; i++) {\n  sequence.push(i * ${index * 5});\n}\nconsole.log("Generated count:", sequence.length);`;
    } else if (topic === 'functions') {
      code = `function calcNetWpm_${index}(grossWpm, errors) {\n  const penalty = errors * 2;\n  return Math.max(0, grossWpm - penalty);\n}`;
    } else if (topic === 'arrays') {
      const tags = ['typing', 'speed', 'drill', 'focus', 'accuracy'];
      code = `const locales = ["uzl", "uzc", "ru", "en"];\nconst activeTags = ["core", "${tags[index - 1]}", "batch_${index}"];`;
    } else if (topic === 'objects') {
      const themes = ['dark', 'dracula', 'nord', 'monokai', 'solarized'];
      code = `const userPreferences = {\n  theme: "${themes[index - 1]}",\n  fontSize: ${14 + index * 2},\n  caretStyle: "line",\n  strictIndent: false,\n};`;
    } else {
      // strings-and-template-literals
      const ports = [8080, 8443, 9000, 9200, 9443];
      code = `const service = "auth-cluster";\nconst port = ${ports[index - 1]};\nconst uri = \`https://\${service}.internal:\${port}/v1/health\`;`;
    }
  } else if (diff === 'INTERMEDIATE') {
    title = `JavaScript ${topic} Functional Utility ${index}`;
    noteEn = `Intermediate ES6+ modern JavaScript pattern using ${topic}.`;
    noteUzl = `Zamonaviy ES6+ uslubidagi ${topic} boʻyicha oʻrta murakkablikdagi kod.`;
    noteRu = `Промежуточный паттерн современного JavaScript ES6+ по теме ${topic}.`;

    const targetWpms = [35, 40, 45, 50, 55];
    code = `export function sanitize${pascalTopic}_${index}(payload) {\n  const { wpm = 0, accuracy = 100, durationMs = 0 } = payload;\n  if (durationMs < 0 || wpm < 0) {\n    throw new Error(\n      "Invalid negative telemetry dimensions"\n    );\n  }\n  const isPassable = wpm >= ${targetWpms[index - 1]} && accuracy >= 95.0;\n  return {\n    wpm: Math.round(wpm),\n    accuracy: Number(accuracy.toFixed(1)),\n    duration: Math.round(durationMs / 1000),\n    isPassable,\n  };\n}`;
  } else if (diff === 'ADVANCED') {
    title = `JavaScript ${topic} Reactive Store Manager ${index}`;
    noteEn = `Advanced JavaScript async telemetry aggregator with listeners.`;
    noteUzl = `Asinxron telemetriya va hodisalar boshqaruviga ega murakkab JavaScript moduli.`;
    noteRu = `Продвинутый асинхронный модуль JavaScript для агрегации телеметрии.`;

    const maxBatches = [25, 50, 75, 100, 125];
    code = `export class ${pascalTopic}Emitter${index} {\n  #subscribers = new Map();\n  #bufferedEvents = [];\n  #maxBatchSize = ${maxBatches[index - 1]};\n\n  subscribe(eventType, callback) {\n    if (typeof callback !== "function") {\n      throw new TypeError(\n        "Subscriber listener must be an executable function"\n      );\n    }\n    if (!this.#subscribers.has(eventType)) {\n      this.#subscribers.set(eventType, new Set());\n    }\n    this.#subscribers.get(eventType).add(callback);\n    return () => this.#subscribers.get(eventType)?.delete(callback);\n  }\n\n  emit(eventType, payload) {\n    const eventObject = {\n      type: eventType,\n      data: payload,\n      timestamp: Date.now(),\n    };\n    this.#bufferedEvents.push(eventObject);\n    const listeners = this.#subscribers.get(eventType);\n    if (listeners) {\n      for (const listener of listeners) {\n        try {\n          listener(eventObject);\n        } catch (err) {\n          console.error("Telemetry error:", err);\n        }\n      }\n    }\n  }\n}`;
  } else {
    // EXPERT
    title = `JavaScript ${topic} High-Performance Engine Worker ${index}`;
    noteEn = `Production-grade real-time keystroke replayer and state machine.`;
    noteUzl = `Yuqori tezlikdagi real-vaqt rejimida klavishlar ijrosi va holatlar mashinasi.`;
    noteRu = `Высокопроизводительный движок воспроизведения нажатий клавиш промышленного уровня.`;

    code = `export class ${pascalTopic}Replayer${index} {\n  #events = [];\n  #cursor = 0;\n  #playbackRate = 1.0;\n  #timerId = null;\n  #onTickCallback = null;\n\n  constructor(events, options = {}) {\n    this.#events = Array.isArray(events) ? [...events] : [];\n    this.#playbackRate = options.speed ?? 1.0;\n    this.#onTickCallback = options.onTick ?? (() => {});\n  }\n\n  start() {\n    if (this.#timerId !== null || this.#events.length === 0) return;\n    let lastTimestamp = performance.now();\n\n    const tick = (now) => {\n      const elapsedDelta = (now - lastTimestamp) * this.#playbackRate;\n      while (\n        this.#cursor < this.#events.length &&\n        (this.#events[this.#cursor]?.t ?? 0) <= elapsedDelta\n      ) {\n        this.#onTickCallback(\n          this.#events[this.#cursor],\n          this.#cursor\n        );\n        this.#cursor += 1;\n      }\n\n      if (this.#cursor < this.#events.length) {\n        this.#timerId = requestAnimationFrame(tick);\n      } else {\n        this.stop();\n      }\n    };\n\n    this.#timerId = requestAnimationFrame(tick);\n  }\n\n  stop() {\n    if (this.#timerId !== null) {\n      cancelAnimationFrame(this.#timerId);\n      this.#timerId = null;\n    }\n  }\n}`;
  }

  code = fitCodeToRange(code, diff, '//');

  return {
    language: 'javascript',
    topic,
    difficulty: diff,
    index,
    type,
    title,
    noteEn,
    noteUzl,
    noteRu,
    tags: ['javascript', topic, diff.toLowerCase()],
    rankedPool: isRanked,
    code,
  };
}
