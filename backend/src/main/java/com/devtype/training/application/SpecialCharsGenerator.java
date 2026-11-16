package com.devtype.training.application;

import com.devtype.content.api.ExerciseCatalog;
import com.devtype.content.api.ExerciseView;
import com.devtype.training.domain.CharCategory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpecialCharsGenerator {

    private final ExerciseCatalog exerciseCatalog;

    // Tilga xos shablonlar
    private static final Map<String, Map<String, List<String>>> TEMPLATES = new HashMap<>();

    static {
        // Java
        Map<String, List<String>> javaT = new HashMap<>();
        javaT.put("BRACKETS", List.of(
                "items.get(index);",
                "new ArrayList<String>();",
                "int[] numbers = new int[]{1, 2, 3};",
                "Map<String, List<Integer>> map;",
                "if ((val > 0) && (val < 100)) {"
        ));
        javaT.put("ASSIGN_CMP", List.of(
                "sum += count * 2;",
                "boolean isOk = (a == b) && (c != d);",
                "total = count <= max ? count : max;",
                "result = (x >= 0) ? x : -x;"
        ));
        javaT.put("PUNCT", List.of(
                "System.out.println(text);",
                "this.name = name; this.id = id;",
                "double val = 3.14159, factor = 2.0;",
                "item.process().save();"
        ));
        javaT.put("LOGICAL", List.of(
                "if (!isValid || (count > 0 && enabled))",
                "String label = (name != null) ? name : \"guest\";",
                "return (a && b) || (!c && d);"
        ));
        javaT.put("ARITH", List.of(
                "int result = (a + b) * (c - d) / 2;",
                "count++; index--;",
                "total += (amount * rate) % 100;"
        ));
        TEMPLATES.put("java", javaT);

        // Python
        Map<String, List<String>> pyT = new HashMap<>();
        pyT.put("BRACKETS", List.of(
                "items = [x for x in data if x > 0]",
                "point = (coords[0], coords[1])",
                "config = {\"host\": \"localhost\", \"port\": 8080}",
                "result = func(a, b, key=lambda x: x[1])"
        ));
        pyT.put("ASSIGN_CMP", List.of(
                "total += count * 2",
                "is_valid = (a == b) and (c != d)",
                "val = count if count <= limit else limit",
                "result = [x for x in items if x >= 0]"
        ));
        pyT.put("LOGICAL", List.of(
                "if not is_valid or (count > 0 and enabled):",
                "value = name if name is not None else default",
                "return (a and b) or (not c and d)"
        ));
        TEMPLATES.put("python", pyT);

        // JavaScript / TypeScript
        Map<String, List<String>> jsT = new HashMap<>();
        jsT.put("BRACKETS", List.of(
                "const [first, ...rest] = items;",
                "const config: Record<string, number[]> = {};",
                "const result = list.filter((x) => x.id === id);",
                "return { data: [item], count: 1 };"
        ));
        jsT.put("ASSIGN_CMP", List.of(
                "sum += count * 2;",
                "const isOk = (a === b) && (c !== d);",
                "total = count <= max ? count : max;",
                "value = input ?? fallback;"
        ));
        jsT.put("ARROWS", List.of(
                "const add = (a: number, b: number) => a + b;",
                "items.map((x) => x.trim()).filter((x) => x.length > 0);",
                "const handler = async () => { await save(); };"
        ));
        TEMPLATES.put("javascript", jsT);
        TEMPLATES.put("typescript", jsT);

        // PL/SQL
        Map<String, List<String>> plsqlT = new HashMap<>();
        plsqlT.put("ASSIGN_CMP", List.of(
                "v_total := v_count * 2;",
                "IF v_a <> v_b AND v_c >= v_d THEN",
                "v_status := 'ACTIVE';",
                "v_id := seq_orders.NEXTVAL;"
        ));
        plsqlT.put("BRACKETS", List.of(
                "OPEN c_emp(p_dept_id);",
                "FETCH c_emp BULK COLLECT INTO l_ids LIMIT 100;",
                "CLOSE c_emp;",
                "FOR i IN 1..l_ids.COUNT LOOP"
        ));
        plsqlT.put("PUNCT", List.of(
                "v_emp_id employees.employee_id%TYPE;",
                "v_rec employees%ROWTYPE;",
                "DBMS_OUTPUT.PUT_LINE(v_total);",
                "COMMIT;"
        ));
        TEMPLATES.put("plsql", plsqlT);

        // SQL
        Map<String, List<String>> sqlT = new HashMap<>();
        sqlT.put("ASSIGN_CMP", List.of(
                "WHERE status = 'ACTIVE' AND amount >= 100.00;",
                "SET balance = balance + 50 WHERE id = 1;",
                "HAVING COUNT(*) > 5 AND SUM(val) <> 0;"
        ));
        sqlT.put("BRACKETS", List.of(
                "SELECT COALESCE(name, 'N/A') FROM users;",
                "WHERE id IN (1, 2, 3, 4, 5);",
                "COUNT(DISTINCT user_id)"
        ));
        TEMPLATES.put("sql", sqlT);
    }

    public SpecialCharsGenerator(ExerciseCatalog exerciseCatalog) {
        this.exerciseCatalog = exerciseCatalog;
    }

    public String generate(String languageSlug, String category, long seed) {
        String lang = languageSlug.toLowerCase().trim();
        String cat = (category != null && !category.isBlank()) ? category.toUpperCase().trim() : "BRACKETS";

        Random rng = new Random(seed);
        List<String> chosenLines = new ArrayList<>();

        // 1. Haqiqiy snippet qatorlaridan olish (density >= 0.25)
        List<ExerciseView> published = exerciseCatalog.findByLanguageAndLevel(lang, "BEGINNER");
        List<String> pool = new ArrayList<>();

        for (ExerciseView ev : published) {
            String[] lines = ev.code().split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.length() >= 15 && trimmed.length() <= 60) {
                    double density = calculateDensity(trimmed, cat);
                    if (density >= 0.25) {
                        pool.add(trimmed);
                    }
                }
            }
        }

        int targetChars = 150;
        int currentChars = 0;

        // 60% pool'dan (taxminan 90 ta belgi)
        if (!pool.isEmpty()) {
            Collections.shuffle(pool, rng);
            for (String line : pool) {
                if (currentChars >= 90) break;
                chosenLines.add(line);
                currentChars += line.length() + 1; // + \n
            }
        }

        // 40% shablonlardan
        Map<String, List<String>> langTemplates = TEMPLATES.getOrDefault(lang, TEMPLATES.get("java"));
        List<String> catTemplates = langTemplates.getOrDefault(cat, langTemplates.values().iterator().next());

        List<String> availableTemplates = new ArrayList<>(catTemplates);
        Collections.shuffle(availableTemplates, rng);

        int tIdx = 0;
        while (currentChars < targetChars && !availableTemplates.isEmpty()) {
            String t = availableTemplates.get(tIdx % availableTemplates.size());
            chosenLines.add(t);
            currentChars += t.length() + 1;
            tIdx++;
            if (tIdx > 20) break; // Cheksiz sikldan himoya
        }

        Collections.shuffle(chosenLines, rng);

        // Matnni birlashtirish va 140..160 belgiga moslash
        StringBuilder sb = new StringBuilder();
        for (String line : chosenLines) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
            if (sb.length() >= 140) {
                break;
            }
        }

        // Agar hali ham 140 ga yetmasa, shablondan qo'shamiz
        while (sb.length() < 140) {
            String t = catTemplates.get(rng.nextInt(catTemplates.size()));
            if (sb.length() > 0) sb.append("\n");
            sb.append(t);
        }

        return sb.toString();
    }

    private double calculateDensity(String line, String category) {
        if (line.isEmpty()) return 0.0;
        int targetCount = 0;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            CharCategory c = CharCategory.ofSingleChar(ch);
            if (c.name().equalsIgnoreCase(category)) {
                targetCount++;
            }
        }
        return (double) targetCount / line.length();
    }
}
