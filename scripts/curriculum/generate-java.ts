import { writeSnippet, fitCodeToRange } from '../generate-curriculum-800';

export function generateJavaCurriculum(): void {
  const topics = [
    'variables',
    'operators',
    'conditions',
    'loops',
    'methods',
    'classes-and-objects',
    'oop',
    'collections',
  ];

  for (const topic of topics) {
    for (const diff of ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as const) {
      for (let i = 1; i <= 5; i++) {
        const snippet = createJavaSnippet(topic, diff, i);
        writeSnippet(snippet);
      }
    }
  }
}

function toPascalCase(str: string): string {
  return str.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('');
}

function createJavaSnippet(
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
    title = `Java ${topic.toUpperCase()} Basic Syntax ${index}`;
    noteEn = `Beginner drill for Java ${topic} language features.`;
    noteUzl = `Java ${topic} boʻyicha boshlangʻich darajadagi sintaktik mashq.`;
    noteRu = `Базовое упражнение по синтаксису Java ${topic}.`;

    if (topic === 'variables') {
      const regions = ['us-east-1', 'us-west-2', 'eu-central-1', 'ap-south-1', 'sa-east-1'];
      code = `final int maxBatchSize = ${index * 50};\nfinal double baseRate = 12.5 * ${index};\nboolean isOnline = true;\nString region = "${regions[index - 1]}";`;
    } else if (topic === 'operators') {
      code = `int counter = ${index * 15};\ncounter += 10;\nboolean isValid = (counter >= 20) && (counter <= 200);\ndouble ratio = (double) counter / 4.0;`;
    } else if (topic === 'conditions') {
      const lats = [85, 95, 110, 135, 160];
      code = `int latencyMs = ${lats[index - 1]};\nif (latencyMs < 100) {\n    System.out.println("Optimal");\n} else {\n    System.out.println("Alert");\n}`;
    } else if (topic === 'loops') {
      code = `int totalSum = 0;\nfor (int i = 1; i <= 5; i++) {\n    totalSum += (i * ${index * 10});\n}\nSystem.out.println("Sum: " + totalSum);`;
    } else if (topic === 'methods') {
      code = `public static int calcDiscount${index}(int price, int percent) {\n    int discount = (price * percent) / 100;\n    return price - discount;\n}`;
    } else if (topic === 'classes-and-objects') {
      code = `public class ServerNode${index} {\n    private final String hostname = "node-${index}.prod";\n    private int connectionCount = ${index * 10};\n}`;
    } else if (topic === 'oop') {
      code = `public interface PaymentGateway${index} {\n    boolean processTransaction(String txId, double amount);\n}`;
    } else {
      // collections
      const tags = ['backend', 'database', 'security', 'caching', 'microservice'];
      code = `List<String> tags = new ArrayList<>();\ntags.add("java");\ntags.add("${tags[index - 1]}");\ntags.add("performance-${index}");`;
    }
  } else if (diff === 'INTERMEDIATE') {
    title = `Java ${topic.toUpperCase()} Idiomatic Pattern ${index}`;
    noteEn = `Intermediate production pattern utilizing Java ${topic}.`;
    noteUzl = `Java ${topic} imkoniyatlaridan foydalanuvchi oʻrta darajadagi amaliy shablon.`;
    noteRu = `Промежуточный практический шаблон на Java по теме ${topic}.`;

    const minVals = [20.0, 30.0, 40.0, 50.0, 60.0];
    const tiers = ['SILVER', 'GOLD', 'PLATINUM', 'VIP', 'DIAMOND'];
    code = `public class ${pascalTopic}Service${index} {\n    private final int maxRetries = ${index + 1};\n    private final double minimumOrderValue = ${minVals[index - 1].toFixed(2)};\n\n    public boolean validateAndExecute(double amount, String tier) {\n        if (amount < minimumOrderValue) {\n            return false;\n        }\n        boolean hasPriority = "${tiers[index - 1]}".equalsIgnoreCase(tier);\n        return hasPriority || amount >= 100.00;\n    }\n}`;
  } else if (diff === 'ADVANCED') {
    title = `Java ${topic.toUpperCase()} Enterprise Architecture ${index}`;
    noteEn = `Advanced robust class structure showcasing clean OOP and collections.`;
    noteUzl = `Toza arxitektura va kolleksiyalar bilan murakkab Java sinfi.`;
    noteRu = `Продвинутая структура классов Java с надежной обработкой данных.`;

    const capSizes = [500, 1000, 1500, 2000, 2500];
    code = `public final class ${pascalTopic}Manager${index} {\n    private final Map<String, Double> settledLedger =\n        new ConcurrentHashMap<>(${capSizes[index - 1]});\n    private final AtomicInteger processedCounter =\n        new AtomicInteger(0);\n\n    public boolean recordSettlement(String txId, double grossAmount) {\n        Objects.requireNonNull(txId, "Transaction ID required");\n        if (grossAmount <= 0.0) {\n            throw new IllegalArgumentException(\n                "Settlement amount must be positive"\n            );\n        }\n\n        settledLedger.put(txId, grossAmount);\n        int currentTotal = processedCounter.incrementAndGet();\n        return currentTotal > 0;\n    }\n\n    public OptionalDouble fetchSettlement(String txId) {\n        Double amount = settledLedger.get(txId);\n        return amount != null\n            ? OptionalDouble.of(amount)\n            : OptionalDouble.empty();\n    }\n}`;
  } else {
    // EXPERT
    title = `Java ${topic.toUpperCase()} Distributed Framework Component ${index}`;
    noteEn = `Production-grade distributed system handler with concurrent thread safety.`;
    noteUzl = `Yuqori yuklamali tizimlar uchun oqimlar xavfsizligiga ega korporativ Java komponenti.`;
    noteRu = `Высоконагруженный распределенный компонент с потокобезопасной архитектурой.`;

    const durations = [15, 30, 45, 60, 90];
    code = `public class ${pascalTopic}Coordinator${index}<K, V> {\n    private final ConcurrentMap<K, CompletableFuture<V>> inFlight;\n    private final Duration cacheEvictionTtl;\n    private final MeterRegistry registry;\n\n    public ${pascalTopic}Coordinator${index}(\n        Duration ttl,\n        MeterRegistry registry\n    ) {\n        this.inFlight = new ConcurrentHashMap<>();\n        this.cacheEvictionTtl = Objects.requireNonNull(ttl, "TTL required");\n        this.registry = Objects.requireNonNull(registry, "Registry required");\n    }\n\n    public CompletableFuture<V> computeIfAbsentAsync(\n        K key,\n        Function<K, CompletableFuture<V>> loader\n    ) {\n        Objects.requireNonNull(key, "Cache key required");\n        Objects.requireNonNull(loader, "Loader required");\n\n        return inFlight.computeIfAbsent(key, targetKey -> {\n            Timer.Sample timer = Timer.start(registry);\n            return loader.apply(targetKey)\n                .whenComplete((result, throwable) -> {\n                    timer.stop(\n                        registry.timer("${topic}.dur.${index}")\n                    );\n                    inFlight.remove(targetKey);\n                });\n        });\n    }\n\n    public int activeCount() {\n        return inFlight.size();\n    }\n}`;
  }

  code = fitCodeToRange(code, diff, '//');

  return {
    language: 'java',
    topic,
    difficulty: diff,
    index,
    type,
    title,
    noteEn,
    noteUzl,
    noteRu,
    tags: ['java', topic, diff.toLowerCase()],
    rankedPool: isRanked,
    code,
  };
}
