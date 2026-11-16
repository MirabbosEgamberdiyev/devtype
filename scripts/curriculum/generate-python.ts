import { writeSnippet, fitCodeToRange } from '../generate-curriculum-800';

export function generatePythonCurriculum(): void {
  const topics = [
    'variables-and-types',
    'operators',
    'conditions',
    'loops',
    'functions',
    'strings-and-fstrings',
    'lists-and-tuples',
    'dictionaries-and-sets',
  ];

  for (const topic of topics) {
    for (const diff of ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as const) {
      for (let i = 1; i <= 5; i++) {
        const snippet = createPythonSnippet(topic, diff, i);
        writeSnippet(snippet);
      }
    }
  }
}

function toPascalCase(str: string): string {
  return str.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('');
}

function createPythonSnippet(
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
  const snakeTopic = topic.replace(/-/g, '_');

  if (['variables-and-types', 'operators', 'conditions', 'loops'].includes(topic) && diff === 'BEGINNER') {
    type = 'SPECIAL_CHARS';
  }

  if (diff === 'BEGINNER') {
    title = `Python ${topic} Basic Drill ${index}`;
    noteEn = `Beginner drill for Python ${topic}.`;
    noteUzl = `Python ${topic} boʻyicha boshlangʻich darajadagi amaliy mashq.`;
    noteRu = `Базовое упражнение по Python ${topic}.`;

    if (topic === 'variables-and-types') {
      const services = ['payment-auth', 'order-router', 'user-sync', 'audit-logger', 'billing-hub'];
      code = `server_port: int = 808${index}\nmax_conns: int = ${index * 50}\nis_healthy: bool = True\nservice_name: str = "${services[index - 1]}"`;
    } else if (topic === 'operators') {
      code = `base_score: int = 100\nbonus: int = 15 * ${index}\ntotal: int = base_score + bonus\nis_valid: bool = (total >= 100) and (total < 500)`;
    } else if (topic === 'conditions') {
      const latencies = [42.5, 68.0, 95.5, 120.0, 155.0];
      code = `latency_ms: float = ${latencies[index - 1]}\nif latency_ms < 50.0:\n    status = "EXCELLENT"\nelif latency_ms < 150.0:\n    status = "ACCEPTABLE"\nelse:\n    status = "DEGRADED"`;
    } else if (topic === 'loops') {
      code = `processed_items: list[int] = []\nfor item_id in range(1, 6):\n    processed_items.append(item_id * ${index * 10})\nprint(f"Total: {len(processed_items)}")`;
    } else if (topic === 'functions') {
      code = `def calculate_discount_${index}(price: float, rate: float) -> float:\n    discount_amount = price * rate\n    return round(price - discount_amount, 2)`;
    } else if (topic === 'strings-and-fstrings') {
      const roles = ['SYS_ADMIN', 'DEV_LEAD', 'AUDITOR', 'OPERATOR', 'ENGINEER'];
      code = `user_id: int = 104${index}\nrole: str = "${roles[index - 1]}"\nlog_entry: str = f"AUDIT: user={user_id} role={role} status=OK"`;
    } else if (topic === 'lists-and-tuples') {
      const clusters = ['us-east', 'eu-west', 'ap-south', 'sa-east', 'ca-central'];
      code = `regions: tuple[str, ...] = ("${clusters[index - 1]}-1", "${clusters[index - 1]}-2")\nactive_nodes: list[str] = ["node-a", "node-b", "node-${index}"]`;
    } else {
      // dictionaries-and-sets
      const scopes = ['FULL_ACCESS', 'WRITE_PRIVILEGE', 'READ_AUDIT', 'OPS_EXECUTE', 'DATA_VIEW'];
      code = `user_roles: dict[str, str] = {\n    "admin": "${scopes[index - 1]}",\n    "editor": "CONTENT_WRITE",\n    "tier_${index}": "STANDARD_ACCESS",\n}`;
    }
  } else if (diff === 'INTERMEDIATE') {
    title = `Python ${topic} Production Utility ${index}`;
    noteEn = `Intermediate Python snippet applying idiomatic typing and pattern matching.`;
    noteUzl = `Python tip annotatsiyalari va funksional uslubdagi oʻrta darajadagi dastur.`;
    noteRu = `Промежуточный сниппет на Python с типизацией и обработкой структур.`;

    const envs = ['staging', 'production', 'development', 'sandbox', 'canary'];
    code = `from typing import Optional\n\ndef configure_${snakeTopic}_${index}(\n    env_name: str = "${envs[index - 1]}",\n    replica_count: int = ${index + 1},\n    debug_mode: bool = False\n) -> dict[str, object]:\n    normalized_env = env_name.strip().lower()\n    if replica_count <= 0:\n        raise ValueError("Replica count must be strictly positive")\n\n    return {\n        "environment": normalized_env,\n        "replicas": replica_count,\n        "debug": debug_mode,\n        "is_prod": normalized_env == "production",\n    }`;
  } else if (diff === 'ADVANCED') {
    title = `Python ${topic} Asynchronous Service ${index}`;
    noteEn = `Advanced production class utilizing context managers and dataclasses.`;
    noteUzl = `Data-classlar va kontekst menejerlari bilan murakkab Python arxitekturasi.`;
    noteRu = `Продвинутый класс Python с контекстными менеджерами и dataclass.`;

    code = `from dataclasses import dataclass, field\nfrom datetime import datetime, timezone\nfrom typing import Any\n\n@dataclass\nclass ${pascalTopic}Tracker${index}:\n    session_id: str\n    user_id: int\n    created_at: datetime = field(\n        default_factory=lambda: datetime.now(timezone.utc)\n    )\n    events: list[dict[str, Any]] = field(default_factory=list)\n\n    def record_keystroke(self, char: str, delta_ms: int) -> None:\n        if delta_ms < 0:\n            raise ValueError("Time delta cannot be negative")\n        self.events.append({\n            "char": char,\n            "dt": delta_ms,\n            "ts": datetime.now(timezone.utc).isoformat(),\n        })\n\n    def calculate_raw_speed(self) -> float:\n        if not self.events:\n            return 0.0\n        total_ms = sum(ev["dt"] for ev in self.events)\n        if total_ms == 0:\n            return 0.0\n        return round((len(self.events) / 5.0) / (total_ms / 60000.0), 2)`;
  } else {
    // EXPERT
    title = `Python ${topic} Resilient Distributed Client ${index}`;
    noteEn = `Production-grade distributed system worker with retry jitter and logging.`;
    noteUzl = `Ishlab chiqarish talablariga mos qayta urinish va monitoringga ega Python moduli.`;
    noteRu = `Корпоративный модуль Python для распределенных систем с защитой от сбоев.`;

    const baseDelays = [0.25, 0.50, 0.75, 1.00, 1.25];
    code = `import asyncio\nimport logging\nfrom typing import Callable, Coroutine, TypeVar\n\nT = TypeVar("T")\nlogger = logging.getLogger("devtype.${snakeTopic}.${index}")\n\nclass ${pascalTopic}Executor${index}:\n    def __init__(self, retries: int = 3, base: float = ${baseDelays[index - 1].toFixed(2)}) -> None:\n        self._retries = retries\n        self._base = base\n\n    async def execute_with_retry(\n        self, operation: Callable[[], Coroutine[object, object, T]]\n    ) -> T:\n        last_error: Exception | None = None\n        for attempt in range(1, self._retries + 1):\n            try:\n                logger.info(f"Execution round {attempt}")\n                return await operation()\n            except (ConnectionError, TimeoutError) as exc:\n                last_error = exc\n                delay = self._base * (2 ** (attempt - 1))\n                logger.warning(\n                    f"Transient failure, retry in {delay}s"\n                )\n                await asyncio.sleep(delay)\n            except Exception as unrecoverable:\n                logger.error(f"Non-transient failure: {unrecoverable}")\n                raise unrecoverable\n\n        raise RuntimeError(\n            f"Operation exceeded retry ceiling: {last_error}"\n        )`;
  }

  code = fitCodeToRange(code, diff, '#');

  return {
    language: 'python',
    topic,
    difficulty: diff,
    index,
    type,
    title,
    noteEn,
    noteUzl,
    noteRu,
    tags: ['python', topic, diff.toLowerCase()],
    rankedPool: isRanked,
    code,
  };
}
