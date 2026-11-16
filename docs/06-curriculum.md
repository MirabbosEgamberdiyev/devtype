# 06. O'quv dasturi (Curriculum)

16 til, jami **231 mavzu**. Har mavzu 4 daraja × kamida 5 snippet → to'liq qamrov uchun kamida **4 620 snippet**. Mavzular tartibi — `sort_order`; admin o'zgartira oladi. Bu ro'yxat `content/` dagi boshlang'ich ma'lumotning manbasi.

## 1. Umumiy qoidalar

### 1.1 Daraja mezonlari (barcha tillar)

| Daraja | Uzunlik | Maxsus belgilar (mo'ljal) | Ichma-ichlik | Mazmun |
| --- | --- | --- | --- | --- |
| `BEGINNER` | 80–200 | < 15% | 0–1 | Bitta konstruksiya, sof ko'rinishda |
| `INTERMEDIATE` | 200–450 | 15–22% | 1–2 | Konstruksiya + odatiy kontekst (funksiya ichida) |
| `ADVANCED` | 450–900 | 22–30% | 2–3 | Bir nechta konstruksiya kombinatsiyasi, real vazifa |
| `EXPERT` | 900–1800 | > 30% | 3+ | Ishlab chiqarish darajasidagi to'liq bo'lak |

Uzunlik qat'iy (validatsiya), qolganlari — mo'ljal (ogohlantirish).

### 1.2 Snippet sifat talablari

1. **Sintaktik to'g'ri va o'z-o'zidan yetarli:** kompilyatsiya/parse qilinadi (kontent CI: Java — `javac`; Python — `ast.parse`; JS/TS — `tsc --noEmit`; SQL — PostgreSQL 16 `PREPARE`; PL/SQL — Oracle Database Free; Go — `gofmt -e`; qolganlari — til parser'i).
2. **Idiomatik:** til hamjamiyatining standart uslubi (Google Java Style, PEP 8, Prettier, `gofmt`, `rustfmt`).
3. **Real:** `foo/bar` emas, domen nomlari (order, user, invoice, employee).
4. **Izohlar:** ko'pi bilan 2 qator, inglizcha.
5. **Bir mavzu — bir fokus:** snippet o'z mavzusining konstruksiyasini kamida 2 marta ishlatadi.
6. **Namunaviy sxema** (SQL, PL/SQL, ORM misollari uchun yagona): `employees`, `departments`, `orders`, `order_items`, `customers`, `products`, `payments`, `audit_log` — DDL `content/schema/` da.

### 1.3 Til konfiguratsiyasi

| Til (slug) | Indent | Kalit so'z registri | `token_patterns` |
| --- | --- | --- | --- |
| `java`, `kotlin`, `csharp`, `c`, `cpp`, `php`, `rust` | 4 probel | lower | `->` `=>` `::` `==` `!=` `<=` `>=` `&&` `\|\|` `++` `--` `+=` `-=` `?.` `?:` `..` |
| `javascript`, `typescript` | 2 probel | lower | `=>` `===` `!==` `==` `!=` `<=` `>=` `&&` `\|\|` `??` `?.` `...` `${` `+=` `++` |
| `python` | 4 probel | lower | `->` `==` `!=` `<=` `>=` `**` `//` `:=` `+=` `-=` |
| `go` | Tab | lower | `:=` `<-` `==` `!=` `<=` `>=` `&&` `\|\|` `...` `+=` `++` |
| `sql` | 2 probel | UPPER | `<>` `!=` `<=` `>=` `\|\|` |
| `plsql` | 2 probel | UPPER | `:=` `=>` `\|\|` `..` `<>` `!=` `<=` `>=` `%TYPE` `%ROWTYPE` `%NOTFOUND` `%FOUND` `%ROWCOUNT` `%ISOPEN` `<<` `>>` |
| `html` | 2 probel | lower | `</` `/>` `<!--` `-->` `="` |
| `css` | 2 probel | lower | `::` `--` `: ` `;` `!important` |
| `bash` | 2 probel | lower | `$(` `${` `[[` `]]` `&&` `\|\|` `>>` `2>&1` `<<` `$?` `\|` |

## 2. Tillar va mavzular

`|` belgisidan oldingi 8 mavzu — R1 (MVP) da; qolganlari R2 da. R2/R3 tillari to'liq dastur bilan chiqadi.

| Til | Reliz | Mavzular (tartib bo'yicha) | Soni |
| --- | --- | --- | --- |
| Java | 1 | Variables and Data Types · Operators · Conditions · Loops · Methods · Classes and Objects · OOP · Collections \| Generics · Streams · Lambda Expressions · Multithreading · Spring Framework · Spring Boot · REST API | 15 |
| Python | 1 | Variables and Types · Operators · Conditions · Loops · Functions · Strings and f-strings · Lists and Tuples · Dictionaries and Sets \| Comprehensions · Classes and OOP · Modules and Imports · Exceptions · Decorators · Generators · Type Hints · async/await | 16 |
| JavaScript | 1 | Variables (let/const) · Operators · Conditions · Loops · Functions and Arrow Functions · Arrays · Objects · Strings and Template Literals \| Destructuring and Spread · Classes · Modules · Promises · async/await · Array Methods · DOM · Fetch API · Error Handling | 17 |
| SQL | 1 | §3 | 16 |
| PL/SQL (Oracle) | 1 | §4 | 18 |
| TypeScript | 2 | Basic Types · Interfaces · Type Aliases · Functions · Classes · Union and Intersection Types · Generics · Enums · Type Guards · Utility Types · Mapped and Conditional Types · Modules · Decorators · Async Types | 14 |
| C# | 2 | Variables and Types · Operators · Control Flow · Methods · Classes and Properties · Interfaces and Inheritance · Collections · Generics · LINQ · Lambdas and Delegates · async/await · Exceptions · Records and Pattern Matching · ASP.NET Core Web API | 14 |
| Go | 2 | Variables and Types · Control Flow · Functions · Slices and Maps · Structs · Methods and Interfaces · Pointers · Error Handling · Goroutines · Channels · Select and Context · Generics · Packages · net/http | 14 |
| Kotlin | 2 | Variables (val/var) · Null Safety · Control Flow · Functions · Classes and Data Classes · Collections · Lambdas and Higher-Order Functions · Extension Functions · Sealed Classes and when · Generics · Coroutines · Flow · Scope Functions · DSL Builders | 14 |
| C++ | 2 | Variables and Types · Control Flow · Functions · References and Pointers · Classes · Constructors and Destructors · Inheritance and Polymorphism · Templates · STL Containers · STL Algorithms · Smart Pointers · Lambdas · Move Semantics · Exceptions · Concurrency | 15 |
| Bash / Shell | 2 | Variables and Quoting · Conditions · Loops · Functions · Pipes and Redirection · Command Substitution · Arrays · Parameter Expansion · grep / sed / awk · Exit Codes and Traps · Script Arguments · Here Documents | 12 |
| C | 3 | Variables and Types · Operators · Conditions · Loops · Functions · Arrays · Pointers · Strings · Structs · Dynamic Memory · File I/O · Preprocessor · Function Pointers · Bitwise Operations | 14 |
| PHP | 3 | Variables and Types · Operators · Control Flow · Functions · Arrays · Strings · Classes and OOP · Namespaces · Traits and Interfaces · Exceptions · PDO · Closures · Type Declarations · Laravel Basics | 14 |
| Rust | 3 | Variables and Mutability · Types · Control Flow · Functions · Ownership and Borrowing · Structs · Enums and Pattern Matching · Option and Result · Collections · Traits · Generics · Lifetimes · Closures and Iterators · Error Handling · Concurrency · Macros | 16 |
| HTML | 3 | Document Structure · Text and Headings · Links and Images · Lists · Tables · Forms and Inputs · Semantic Elements · Media · Meta and SEO · Accessibility (ARIA) | 10 |
| CSS | 3 | Selectors · Box Model · Colors and Units · Typography · Flexbox · Grid · Positioning · Pseudo-classes and Pseudo-elements · Media Queries · Custom Properties · Transitions and Animations · Functions (calc, clamp) | 12 |

MVP kontent hajmi: 5 til × 8 mavzu × 4 daraja × 5 = **800 snippet** + har tilda ≥ 30 ranked pool snippet (ular yuqoridagi `INTERMEDIATE` snippetlar ichidan belgilanadi) + har tilda ≥ 20 `SPECIAL_CHARS` qator to'plami.

---

## 3. SQL yo'nalishi

### 3.1 Tavsif

- Slug `sql`. Dialekt: **ANSI SQL**. Dialektga xos snippet `oracle` yoki `postgres` tegi bilan (`FR-SQL-2`).
- PL/SQL bilan umumiy mashq yo'q; statistikalar alohida.

### 3.2 Formatlash standarti

```sql
SELECT d.department_name,
       COUNT(e.employee_id) AS employee_count,
       ROUND(AVG(e.salary), 2) AS avg_salary
  FROM departments d
  LEFT JOIN employees e
    ON e.department_id = d.department_id
 WHERE d.location_id = 1700
 GROUP BY d.department_name
HAVING COUNT(e.employee_id) > 5
 ORDER BY avg_salary DESC;
```

- Kalit so'zlar va funksiyalar UPPER; identifikatorlar lower snake_case; taxalluslar qisqa (`e`, `d`).
- Har klauza yangi qatorda, kalit so'zlar **o'ng chetga tekislangan** ("river" uslubi); ustunlar ro'yxati birinchi ustun ostida tekislangan.
- Vergul qator oxirida; operatorlar atrofida bitta probel; `AS` har doim yoziladi.
- Operator oxirida `;`.
- Tekislash uchun qator boshidagi barcha bo'shliq — avtomatik indent (foydalanuvchi termaydi).

### 3.3 Mavzular va daraja mazmuni

| # | Mavzu | BEGINNER | INTERMEDIATE | ADVANCED | EXPERT |
| --- | --- | --- | --- | --- | --- |
| 1 | SELECT | Ustunlar, `*`, `AS`, `DISTINCT` | Ifodalar, `CASE`, satr/sana funksiyalari | Skalyar quyi so'rov, `COALESCE`, `CAST` | Ko'p ifodali hisobot so'rovi |
| 2 | WHERE | `=`, `<>`, `AND`/`OR` | `IN`, `BETWEEN`, `LIKE`, `IS NULL` | Murakkab qavsli shartlar, `EXISTS` | Korrelyatsiyalangan filtrlar kombinatsiyasi |
| 3 | ORDER BY | Bitta ustun, `ASC`/`DESC` | Ko'p ustun, ifoda bo'yicha | `NULLS FIRST/LAST`, `CASE` bo'yicha | `FETCH FIRST n ROWS`, sahifalash so'rovlari |
| 4 | INSERT | Bitta qator `VALUES` | Ko'p qatorli `VALUES`, ustun ro'yxati | `INSERT … SELECT` | `INSERT … SELECT` + `JOIN` + `CASE` |
| 5 | UPDATE | Bitta ustun, `WHERE` | Ko'p ustun, ifodalar | Quyi so'rov bilan `SET` | Korrelyatsiyalangan `UPDATE`, `CASE` |
| 6 | DELETE | `WHERE` bilan | `IN` quyi so'rovi | `EXISTS` bilan | Ko'p bosqichli tozalash skripti |
| 7 | JOIN | `INNER JOIN` 2 jadval | `LEFT JOIN`, 3 jadval | `FULL`/`CROSS`, self-join | 5+ jadval, aralash join turlari |
| 8 | GROUP BY | `COUNT`, `SUM` | Ko'p ustun, `AVG`/`MIN`/`MAX` | `ROLLUP`, `GROUPING SETS` | `CUBE`, shartli agregatsiya (`FILTER`/`CASE`) |
| 9 | HAVING | Bitta agregat sharti | Ko'p shart | Quyi so'rov bilan | `HAVING` + `ROLLUP` hisobotlari |
| 10 | Subqueries | `IN (SELECT …)` | Skalyar, `FROM` ichida | Korrelyatsiyalangan, `EXISTS`/`NOT EXISTS` | Ko'p darajali ichma-ich |
| 11 | CTE | Bitta `WITH` | Ko'p CTE zanjiri | Rekursiv CTE (ierarxiya) | Rekursiv + agregatsiya + join |
| 12 | Window Functions | `ROW_NUMBER() OVER (ORDER BY …)` | `PARTITION BY`, `RANK`, `DENSE_RANK` | `LAG`/`LEAD`, `SUM() OVER`, ramkalar | `ROWS BETWEEN`, `NTILE`, bir nechta oyna |
| 13 | MERGE | `WHEN MATCHED THEN UPDATE` | `MATCHED` + `NOT MATCHED` | Quyi so'rov manbali, shartli | `DELETE` shoxi bilan to'liq upsert |
| 14 | Indexes | `CREATE INDEX` | Kompozit, `UNIQUE` | Funksional, qisman (`postgres` tegi) / bitmap (`oracle` tegi) | Indeks strategiyasi skripti (yaratish, qayta qurish, o'chirish) |
| 15 | Execution Plans | `EXPLAIN SELECT …` (`postgres`) / `EXPLAIN PLAN FOR` (`oracle`) | `EXPLAIN (ANALYZE, BUFFERS)` / `DBMS_XPLAN.DISPLAY` | Hint'lar (`/*+ INDEX(…) */`, `oracle`), statistikani yig'ish | Rejani taqqoslash skriptlari, `DBMS_XPLAN.DISPLAY_CURSOR` |
| 16 | Query Optimization | `SELECT *` o'rniga ustunlar, sargable shart | `EXISTS` vs `IN`, keraksiz `DISTINCT` ni olib tashlash | Join tartibi, CTE vs quyi so'rov, oyna funksiyasi bilan qayta yozish | Katta hisobotni bosqichma-bosqich optimallashtirilgan ko'rinishi |

14–15-mavzulardagi dialektga xos snippetlar teg bilan belgilanadi va CI'da tegishli SUBD'da tekshiriladi.

---

## 4. PL/SQL (Oracle) yo'nalishi

### 4.1 Tavsif

- Slug `plsql`. **Faqat Oracle Database 19c+** sintaksisi. PL/pgSQL, T-SQL, MySQL konstruksiyalari taqiqlangan (`BL-EM-3`).
- Har snippet Oracle Database Free konteynerida kompilyatsiya qilinadi (`FR-PLS-6`).
- Blok oxirida alohida qatorda `/` (`FR-PLS-4`).

### 4.2 Formatlash standarti

```sql
CREATE OR REPLACE PROCEDURE raise_salary (
  p_employee_id IN employees.employee_id%TYPE,
  p_percent     IN NUMBER DEFAULT 5
) IS
  v_salary employees.salary%TYPE;
  e_invalid_percent EXCEPTION;
BEGIN
  IF p_percent <= 0 OR p_percent > 50 THEN
    RAISE e_invalid_percent;
  END IF;

  SELECT salary
    INTO v_salary
    FROM employees
   WHERE employee_id = p_employee_id
     FOR UPDATE;

  UPDATE employees
     SET salary = v_salary * (1 + p_percent / 100)
   WHERE employee_id = p_employee_id;

  COMMIT;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20001, 'Employee not found: ' || p_employee_id);
  WHEN e_invalid_percent THEN
    RAISE_APPLICATION_ERROR(-20002, 'Percent must be between 1 and 50');
END raise_salary;
/
```

- Kalit so'zlar, o'rnatilgan funksiyalar va paketlar UPPER (`BEGIN`, `NVL`, `DBMS_OUTPUT.PUT_LINE`); identifikatorlar lower snake_case.
- Indent 2 probel; ichki SQL — §3.2 "river" uslubida.
- Prefikslar: `v_` lokal o'zgaruvchi · `p_` parametr · `c_` kursor **yoki** konstanta (`c_limit CONSTANT …`) · `t_` tur va kolleksiya o'zgaruvchisi · `r_` record · `e_` istisno · `g_` paket darajasidagi global.
- Parametr rejimi har doim yoziladi (`IN`, `OUT`, `IN OUT`); parametrlar ustun bo'lib tekislanadi.
- `END` dan keyin nom takrorlanadi (`END raise_salary;`, `END LOOP;`, `END IF;`).
- Satrlar bitta tirnoqda; ichidagi tirnoq ikkilanadi yoki `q'[...]'`.
- Protsedura chaqiruvida 2 dan ortiq parametr bo'lsa nomlangan notatsiya (`=>`).

### 4.3 Qamrab olinishi shart bo'lgan Oracle konstruksiyalari

| Konstruksiya | Misol | Mavzu |
| --- | --- | --- |
| Tayinlash | `v_count := 0;` | 3 |
| Bog'langan turlar | `v_name employees.last_name%TYPE;` · `r_emp employees%ROWTYPE;` | 3, 12 |
| Birlashtirish | `v_full := v_first \|\| ' ' \|\| v_last;` | 1 |
| Blok tuzilmasi | `DECLARE … BEGIN … EXCEPTION … END;` + `/` | 2 |
| Shartlar | `IF … THEN … ELSIF … ELSE … END IF;` · `CASE … WHEN … END CASE;` | 4 |
| Sikllar | `LOOP … EXIT WHEN …; END LOOP;` · `WHILE … LOOP` · `FOR i IN 1..10 LOOP` · `FOR i IN REVERSE 1..10` · `CONTINUE WHEN` | 5 |
| Yorliqlar | `<<outer_loop>>` · `EXIT outer_loop WHEN …;` | 5 |
| Parametr rejimlari | `p_result OUT NUMBER` · `IN OUT NOCOPY` · `DEFAULT` | 6 |
| Nomlangan notatsiya | `calc_bonus(p_emp_id => 100, p_rate => 0.1);` | 6, 7 |
| Funksiya | `RETURN NUMBER` · `DETERMINISTIC` · `RESULT_CACHE` · `PIPELINED` + `PIPE ROW` | 7, 18 |
| Kursor | `CURSOR c_emp (p_dept NUMBER) IS SELECT …;` · `OPEN` · `FETCH … INTO` · `%NOTFOUND` · `%ROWCOUNT` · `CLOSE` · kursor `FOR` sikli · `SYS_REFCURSOR` · `FOR UPDATE` / `WHERE CURRENT OF` | 8 |
| Istisnolar | `WHEN NO_DATA_FOUND THEN` · `WHEN TOO_MANY_ROWS` · `WHEN OTHERS` · `RAISE` · `RAISE_APPLICATION_ERROR(-20001, '…')` · `SQLCODE` · `SQLERRM` · `PRAGMA EXCEPTION_INIT` | 9 |
| Paket | `CREATE OR REPLACE PACKAGE … AS … END pkg;` · `PACKAGE BODY` · initsializatsiya bloki | 10 |
| Trigger | `BEFORE INSERT OR UPDATE OF salary ON employees FOR EACH ROW` · `:NEW` · `:OLD` · `WHEN (…)` · `INSERTING`/`UPDATING`/`DELETING` · compound trigger | 11 |
| Record va kolleksiyalar | `TYPE r_x IS RECORD (…)` · `TABLE OF … INDEX BY PLS_INTEGER` · nested table · `VARRAY(10)` · `.COUNT` `.FIRST` `.LAST` `.NEXT(i)` `.EXTEND` `.DELETE` `.EXISTS(i)` | 12 |
| Ommaviy amallar | `BULK COLLECT INTO … LIMIT 500;` · `FORALL i IN 1..t.COUNT` · `FORALL i IN INDICES OF t` · `SAVE EXCEPTIONS` · `SQL%BULK_EXCEPTIONS` · `SQL%BULK_ROWCOUNT` | 13, 14 |
| Dinamik SQL | `EXECUTE IMMEDIATE v_sql INTO v_cnt USING p_id;` · `RETURNING INTO` · `OPEN c FOR v_sql USING …` · bind `:1` | 15 |
| DBMS_SQL | `DBMS_SQL.OPEN_CURSOR` · `PARSE(…, DBMS_SQL.NATIVE)` · `BIND_VARIABLE` · `DEFINE_COLUMN` · `EXECUTE` · `FETCH_ROWS` · `COLUMN_VALUE` · `CLOSE_CURSOR` | 16 |
| DBMS_SCHEDULER | `DBMS_SCHEDULER.CREATE_JOB(job_name => …, job_type => 'PLSQL_BLOCK', repeat_interval => 'FREQ=DAILY; BYHOUR=2', enabled => TRUE);` · `RUN_JOB` · `DROP_JOB` · `CREATE_PROGRAM` · `CREATE_SCHEDULE` | 17 |
| Direktivalar va optimallashtirish | `PRAGMA AUTONOMOUS_TRANSACTION;` · `NOCOPY` · `PARALLEL_ENABLE` · `PLS_INTEGER` vs `NUMBER` · `DBMS_PROFILER` / `DBMS_UTILITY.GET_TIME` | 18 |

### 4.4 Mavzular va daraja mazmuni

| # | Mavzu | BEGINNER | INTERMEDIATE | ADVANCED | EXPERT |
| --- | --- | --- | --- | --- | --- |
| 1 | PL/SQL Fundamentals | Anonim blok, `DBMS_OUTPUT.PUT_LINE`, `\|\|` | Ifodalar, `NVL`, `TO_CHAR`, izohlar | Ichma-ich bloklar, ko'rinish sohasi | Yorliqli bloklar, kvalifikatsiyalangan nomlar |
| 2 | DECLARE / BEGIN / END | `DECLARE` + 1–2 o'zgaruvchi | `SELECT … INTO` bilan | `EXCEPTION` bo'limi bilan | Ichma-ich bloklar + lokal quyi dasturlar |
| 3 | Variables and Data Types | `NUMBER`, `VARCHAR2`, `DATE`, `:=` | `%TYPE`, `CONSTANT`, `NOT NULL`, `BOOLEAN` | `%ROWTYPE`, `SUBTYPE`, `TIMESTAMP`, `INTERVAL` | `CLOB`/`BLOB`, foydalanuvchi turlari, konversiyalar |
| 4 | IF / CASE | `IF … THEN … END IF` | `ELSIF`, `ELSE` | `CASE` operatori va ifodasi | Ichma-ich `CASE` + murakkab shartlar |
| 5 | LOOP / WHILE / FOR | Oddiy `LOOP` + `EXIT WHEN` | `WHILE`, sonli `FOR` | `REVERSE`, `CONTINUE`, kursor `FOR` | Yorliqli ichma-ich sikllar |
| 6 | PROCEDURE | Parametrsiz protsedura | `IN` parametrlar, `DEFAULT` | `OUT`, `IN OUT`, nomlangan chaqiruv | `NOCOPY`, overload, `AUTHID` |
| 7 | FUNCTION | Oddiy `RETURN` | Hisoblovchi funksiya, SQL'da chaqirish | `DETERMINISTIC`, ko'p `RETURN` yo'llari | `PIPELINED`, `RESULT_CACHE` |
| 8 | CURSOR | Yashirin kursor (`SQL%ROWCOUNT`) | Aniq kursor: `OPEN`/`FETCH`/`CLOSE` | Parametrli kursor, `FOR UPDATE` | `SYS_REFCURSOR`, `WHERE CURRENT OF` |
| 9 | EXCEPTION Handling | `WHEN NO_DATA_FOUND` | `WHEN OTHERS`, `SQLERRM` | Foydalanuvchi istisnosi, `RAISE_APPLICATION_ERROR` | `PRAGMA EXCEPTION_INIT`, qayta ko'tarish, log protsedurasi |
| 10 | PACKAGE / PACKAGE BODY | Spetsifikatsiya (1 protsedura) | Spec + body | Yopiq a'zolar, paket o'zgaruvchilari | Overload, initsializatsiya, forward declaration |
| 11 | TRIGGER | `BEFORE INSERT … FOR EACH ROW`, `:NEW` | `:OLD`, `UPDATE OF` | `INSERTING`/`UPDATING`, `WHEN` sharti, audit | Compound trigger, `INSTEAD OF` |
| 12 | RECORD / COLLECTION | `%ROWTYPE` record | `TYPE … IS RECORD` | Associative array, metodlar | Nested table, `VARRAY`, `MULTISET`, `TABLE()` |
| 13 | BULK COLLECT | `SELECT … BULK COLLECT INTO` | Kursor + `FETCH … BULK COLLECT` | `LIMIT` bilan sikl | Record kolleksiyalari, `RETURNING BULK COLLECT` |
| 14 | FORALL | `FORALL … INSERT` | `UPDATE`/`DELETE`, `SQL%BULK_ROWCOUNT` | `SAVE EXCEPTIONS` + ishlov | `INDICES OF`/`VALUES OF`, `BULK COLLECT` bilan konveyer |
| 15 | Dynamic SQL | `EXECUTE IMMEDIATE` DDL | `USING`, `INTO` | `RETURNING INTO`, dinamik kursor | Dinamik PL/SQL bloklari, `DBMS_ASSERT` |
| 16 | DBMS_SQL | `OPEN_CURSOR`/`PARSE`/`EXECUTE`/`CLOSE_CURSOR` | `BIND_VARIABLE` | `DEFINE_COLUMN`/`FETCH_ROWS`/`COLUMN_VALUE` | `DESCRIBE_COLUMNS`, `TO_REFCURSOR` |
| 17 | DBMS_SCHEDULER | `CREATE_JOB` (oddiy) | `repeat_interval`, `RUN_JOB`, `DROP_JOB` | `CREATE_PROGRAM` + `CREATE_SCHEDULE` | Zanjirlar, argumentlar, job monitoring so'rovlari |
| 18 | Performance Optimization | `PLS_INTEGER`, sikl ichidan SQL'ni chiqarish | Qatorma-qator → `BULK COLLECT`/`FORALL` | `NOCOPY`, `RESULT_CACHE`, bind o'zgaruvchilar | `PARALLEL_ENABLE`, pipelined, profil o'lchovi |

### 4.5 Namuna (ADVANCED, mavzu 13 — BULK COLLECT)

```sql
DECLARE
  TYPE t_order_ids IS TABLE OF orders.order_id%TYPE;
  t_ids   t_order_ids;
  c_limit CONSTANT PLS_INTEGER := 500;
  CURSOR c_orders IS
    SELECT order_id
      FROM orders
     WHERE status = 'NEW';
BEGIN
  OPEN c_orders;
  LOOP
    FETCH c_orders BULK COLLECT INTO t_ids LIMIT c_limit;
    EXIT WHEN t_ids.COUNT = 0;
    FORALL i IN 1..t_ids.COUNT
      UPDATE orders
         SET status = 'QUEUED'
       WHERE order_id = t_ids(i);
    COMMIT;
  END LOOP;
  CLOSE c_orders;
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END;
/
```

### 4.6 Engine uchun oqibatlar (SQL va PL/SQL)

- Registr qat'iy: `select` yozilsa xato. "Registrni e'tiborsiz qoldirish" sozlamasi yo'q.
- Caps Lock indikatori SQL/PL/SQL da ogohlantirish emas, oddiy holat belgisi (ko'k, sariq emas).
- "Registr" belgi turkumi: katta harflar tezligi va aniqligi alohida ko'rsatiladi.
- `%TYPE` kabi token pattern'lar ichida harflar bor — ular bitta guruh sifatida kuzatiladi (birinchi belgidan oxirgisigacha vaqt).

## 5. Kontent manbasi formati (`content/<lang>/<topic>/<level>-<n>.yaml`)

```yaml
language: plsql
topic: bulk-collect
difficulty: ADVANCED
type: SNIPPET
title: BULK COLLECT with LIMIT
note:
  en: LIMIT bounds memory use when fetching large result sets.
  uzl: LIMIT katta natijalarni o‘qishda xotira sarfini chegaralaydi.
  ru: LIMIT ограничивает расход памяти при выборке больших наборов.
tags: [cursor, bulk, performance]
ranked_pool: false
source: original          # yoki URL + litsenziya
code: |
  DECLARE
    ...
  END;
  /
```

`uzc` izohi import paytida `uzl` dan transliteratsiya qilinadi (`22-i18n.md` §6); fayl ichida berilgan bo'lsa — o'sha ustun.
