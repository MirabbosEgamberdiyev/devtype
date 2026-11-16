import { writeSnippet, fitCodeToRange } from '../generate-curriculum-800';

export function generateSqlCurriculum(): void {
  const topics = [
    'select',
    'where',
    'order-by',
    'group-by',
    'joins',
    'subqueries',
    'insert-update-delete',
    'aggregates',
  ];

  for (const topic of topics) {
    for (const diff of ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as const) {
      for (let i = 1; i <= 5; i++) {
        const snippet = createSqlSnippet(topic, diff, i);
        writeSnippet(snippet);
      }
    }
  }
}

function createSqlSnippet(
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

  if (['select', 'where', 'order-by', 'group-by'].includes(topic) && diff === 'BEGINNER') {
    type = 'SPECIAL_CHARS';
  }

  if (diff === 'BEGINNER') {
    title = `SQL ${topic.toUpperCase()} Basic Drill ${index}`;
    noteEn = `Beginner drill for SQL ${topic} statements.`;
    noteUzl = `SQL ${topic} ifodalari boʻyicha boshlangʻich darajadagi amaliy mashq.`;
    noteRu = `Базовое упражнение для SQL выражений ${topic}.`;

    if (topic === 'select') {
      const colMap = [
        'customer_id, full_name, email, balance',
        'employee_id, first_name, last_name, salary',
        'product_id, product_name, category, unit_price',
        'order_id, customer_id, order_date, total_amount',
        'department_id, department_name, manager_id',
      ];
      const tblMap = ['customers', 'employees', 'products', 'orders', 'departments'];
      code = `SELECT ${colMap[index - 1]}\n  FROM ${tblMap[index - 1]}\n LIMIT 20;`;
    } else if (topic === 'where') {
      const condMap = [
        'status = \'ACTIVE\' AND total_amount > 250.00',
        'salary >= 5000 AND department_id = 10',
        'unit_price BETWEEN 15.00 AND 95.00',
        'is_active = TRUE AND stock_quantity > 0',
        'balance >= 1000.00 AND tier = \'GOLD\'',
      ];
      const tblMap = ['orders', 'employees', 'products', 'products', 'customers'];
      code = `SELECT *\n  FROM ${tblMap[index - 1]}\n WHERE ${condMap[index - 1]};`;
    } else if (topic === 'order-by') {
      const ordMap = [
        'total_amount DESC, order_date ASC',
        'salary DESC, last_name ASC',
        'unit_price ASC, product_name ASC',
        'created_at DESC, customer_id ASC',
        'department_name ASC, department_id ASC',
      ];
      const tblMap = ['orders', 'employees', 'products', 'customers', 'departments'];
      code = `SELECT *\n  FROM ${tblMap[index - 1]}\n ORDER BY ${ordMap[index - 1]}\n LIMIT 50;`;
    } else if (topic === 'group-by') {
      const grpMap = ['status', 'department_id', 'category', 'tier', 'payment_method'];
      const tblMap = ['orders', 'employees', 'products', 'customers', 'payments'];
      code = `SELECT ${grpMap[index - 1]},\n       COUNT(*) AS total_count\n  FROM ${tblMap[index - 1]}\n GROUP BY ${grpMap[index - 1]};`;
    } else if (topic === 'joins') {
      const joins = [
        'employees e\n  JOIN departments d\n    ON d.department_id = e.department_id',
        'orders o\n  JOIN customers c\n    ON c.customer_id = o.customer_id',
        'order_items oi\n  JOIN products p\n    ON p.product_id = oi.product_id',
        'payments pmt\n  JOIN orders o\n    ON o.order_id = pmt.order_id',
        'departments d\n  JOIN employees e\n    ON e.employee_id = d.manager_id',
      ];
      code = `SELECT *\n  FROM ${joins[index - 1]}\n LIMIT 25;`;
    } else if (topic === 'subqueries') {
      const subqs = [
        'SELECT employee_id, salary\n  FROM employees\n WHERE salary > (\n       SELECT AVG(salary)\n         FROM employees\n       );',
        'SELECT customer_id, full_name\n  FROM customers\n WHERE customer_id IN (\n       SELECT customer_id\n         FROM orders\n        WHERE total_amount > 500\n       );',
        'SELECT product_id, product_name\n  FROM products\n WHERE unit_price > (\n       SELECT AVG(unit_price)\n         FROM products\n       );',
        'SELECT department_id, department_name\n  FROM departments\n WHERE department_id IN (\n       SELECT department_id\n         FROM employees\n        WHERE salary > 8000\n       );',
        'SELECT order_id, total_amount\n  FROM orders\n WHERE total_amount < (\n       SELECT AVG(total_amount)\n         FROM orders\n       );',
      ];
      code = subqs[index - 1];
    } else if (topic === 'insert-update-delete') {
      const dmls = [
        'INSERT INTO departments (department_id, department_name)\nVALUES (901, \'Engineering Infrastructure\');',
        'UPDATE employees\n   SET salary = salary * 1.05\n WHERE department_id = 40;',
        'DELETE FROM audit_log\n WHERE performed_at < \'2025-01-01\';',
        'INSERT INTO customers (full_name, email, tier)\nVALUES (\'Temur Aliyev\', \'temur@example.com\', \'BRONZE\');',
        'UPDATE products\n   SET stock_quantity = stock_quantity + 50\n WHERE is_active = TRUE;',
      ];
      code = dmls[index - 1];
    } else {
      // aggregates
      const fns = ['COUNT(*)', 'AVG(salary)', 'SUM(total_amount)', 'MAX(unit_price)', 'MIN(balance)'];
      const tbls = ['customers', 'employees', 'orders', 'products', 'customers'];
      code = `SELECT ${fns[index - 1]} AS aggregate_metric,\n       COUNT(1) AS row_frequency\n  FROM ${tbls[index - 1]};`;
    }
  } else if (diff === 'INTERMEDIATE') {
    title = `SQL ${topic.toUpperCase()} Intermediate Query ${index}`;
    noteEn = `Intermediate real-world ${topic} pattern with multi-column expressions.`;
    noteUzl = `Haqiqiy biznes vazifalarida ${topic} boʻyicha oʻrta darajadagi soʻrov.`;
    noteRu = `Промежуточный практический запрос с использованием ${topic}.`;

    if (topic === 'select') {
      const offsets = [1000, 2000, 3000, 4000, 5000];
      code = `SELECT e.employee_id,\n       e.first_name || ' ' || e.last_name AS employee_name,\n       e.salary,\n       ROUND(e.salary * 12, 2) AS annual_salary,\n       COALESCE(e.commission_pct, 0.0) AS commission_rate\n  FROM employees e\n WHERE e.salary >= ${offsets[index - 1]}\n ORDER BY annual_salary DESC;`;
    } else if (topic === 'where') {
      const thresholds = [500, 750, 1000, 1250, 1500];
      code = `SELECT o.order_id,\n       o.customer_id,\n       o.total_amount,\n       o.status\n  FROM orders o\n WHERE (o.status = 'PENDING' AND o.total_amount >= ${thresholds[index - 1]}.00)\n    OR (o.status = 'REVIEW' AND o.order_date >= '2026-01-01')\n ORDER BY o.total_amount DESC;`;
    } else if (topic === 'order-by') {
      const cats = ['ELECTRONICS', 'BOOKS', 'CLOTHING', 'OFFICE', 'HARDWARE'];
      code = `SELECT p.product_id,\n       p.product_name,\n       p.category,\n       p.unit_price,\n       p.stock_quantity\n  FROM products p\n WHERE p.category = '${cats[index - 1]}'\n ORDER BY p.unit_price DESC,\n          p.stock_quantity ASC\n LIMIT 100;`;
    } else if (topic === 'group-by') {
      const mins = [1, 2, 3, 4, 5];
      code = `SELECT d.department_name,\n       COUNT(e.employee_id) AS employee_count,\n       ROUND(AVG(e.salary), 2) AS avg_salary,\n       MAX(e.salary) AS peak_salary\n  FROM departments d\n  JOIN employees e\n    ON e.department_id = d.department_id\n GROUP BY d.department_name\nHAVING COUNT(e.employee_id) >= ${mins[index - 1]}\n ORDER BY avg_salary DESC;`;
    } else if (topic === 'joins') {
      const statuses = ['DELIVERED', 'SHIPPED', 'PROCESSING', 'APPROVED', 'DISPATCHED'];
      code = `SELECT o.order_id,\n       c.full_name AS customer_name,\n       p.product_name,\n       oi.quantity,\n       oi.unit_price\n  FROM orders o\n  JOIN customers c\n    ON c.customer_id = o.customer_id\n  JOIN order_items oi\n    ON oi.order_id = o.order_id\n  JOIN products p\n    ON p.product_id = oi.product_id\n WHERE o.status = '${statuses[index - 1]}';`;
    } else if (topic === 'subqueries') {
      const tiers = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'VIP'];
      code = `SELECT c.customer_id,\n       c.full_name,\n       c.balance\n  FROM customers c\n WHERE c.tier = '${tiers[index - 1]}'\n   AND c.customer_id IN (\n       SELECT o.customer_id\n         FROM orders o\n        WHERE o.status = 'COMPLETED'\n       )\n ORDER BY c.balance DESC;`;
    } else if (topic === 'insert-update-delete') {
      const locs = [1700, 1800, 1900, 2000, 2100];
      code = `UPDATE employees e\n   SET salary = salary * 1.08,\n       commission_pct = COALESCE(commission_pct, 0.05)\n WHERE e.department_id IN (\n       SELECT d.department_id\n         FROM departments d\n        WHERE d.location_id = ${locs[index - 1]}\n       )\n   AND e.salary <= 6000;`;
    } else {
      // aggregates
      const minStocks = [10, 20, 30, 40, 50];
      code = `SELECT p.category,\n       COUNT(p.product_id) AS total_products,\n       ROUND(AVG(p.unit_price), 2) AS average_price,\n       SUM(p.stock_quantity * p.unit_price) AS inventory_value\n  FROM products p\n WHERE p.is_active = TRUE\n GROUP BY p.category\nHAVING SUM(p.stock_quantity) > ${minStocks[index - 1]}\n ORDER BY inventory_value DESC;`;
    }
  } else if (diff === 'ADVANCED') {
    title = `SQL ${topic.toUpperCase()} Advanced Pipeline ${index}`;
    noteEn = `Advanced analytical ${topic} pipeline with complex joins and aggregations.`;
    noteUzl = `Murakkab birlashmalar va guruhlashlar bilan ${topic} boʻyicha chuqurlashtirilgan soʻrov.`;
    noteRu = `Продвинутый аналитический конвейер с использованием ${topic}.`;

    const deptMinSal = [2500, 3000, 3500, 4000, 4500];
    const yr = [2019, 2020, 2021, 2022, 2023];

    if (topic === 'select') {
      code = `SELECT e.employee_id,\n       e.first_name || ' ' || e.last_name AS employee_name,\n       e.salary,\n       ROUND(e.salary * 1.12, 2) AS projected_salary,\n       CASE\n         WHEN e.salary >= 6000 THEN 'SENIOR_BAND'\n         WHEN e.salary >= 3500 THEN 'MID_BAND'\n         ELSE 'ENTRY_BAND'\n       END AS compensation_tier,\n       d.department_name,\n       COALESCE(d.location_id, 1000) AS resolved_location\n  FROM employees e\n  LEFT JOIN departments d\n    ON d.department_id = e.department_id\n WHERE e.salary >= ${deptMinSal[index - 1]}\n ORDER BY projected_salary DESC,\n          e.employee_id ASC;`;
    } else if (topic === 'where') {
      code = `SELECT o.order_id,\n       o.customer_id,\n       o.total_amount,\n       o.order_date,\n       o.status\n  FROM orders o\n WHERE o.total_amount BETWEEN ${deptMinSal[index - 1]}.00 AND 10000.00\n   AND o.order_date >= '${yr[index - 1]}-01-01'\n   AND (o.status = 'COMPLETED' OR o.status = 'PROCESSING')\n   AND EXISTS (\n       SELECT 1\n         FROM order_items oi\n        WHERE oi.order_id = o.order_id\n          AND oi.quantity >= 2\n       )\n ORDER BY o.total_amount DESC;`;
    } else if (topic === 'order-by') {
      code = `SELECT p.product_id,\n       p.product_name,\n       p.category,\n       p.unit_price,\n       p.stock_quantity,\n       p.unit_price * p.stock_quantity AS total_inventory_cost\n  FROM products p\n WHERE p.unit_price >= ${index * 15}.00\n ORDER BY p.category ASC,\n          total_inventory_cost DESC,\n          p.unit_price DESC,\n          p.product_id ASC\n LIMIT 100;`;
    } else if (topic === 'group-by') {
      code = `SELECT d.department_name,\n       e.job_id,\n       COUNT(e.employee_id) AS total_headcount,\n       ROUND(AVG(e.salary), 2) AS average_salary,\n       SUM(e.salary) AS total_payroll\n  FROM departments d\n  JOIN employees e\n    ON e.department_id = d.department_id\n WHERE e.hire_date >= '${yr[index - 1]}-01-01'\n GROUP BY d.department_name,\n          e.job_id\nHAVING COUNT(e.employee_id) >= ${index}\n   AND SUM(e.salary) > 4000\n ORDER BY total_payroll DESC;`;
    } else if (topic === 'joins') {
      code = `SELECT o.order_id,\n       c.full_name AS customer_name,\n       c.email AS customer_email,\n       p.product_name,\n       oi.quantity,\n       oi.unit_price,\n       ROUND(oi.quantity * oi.unit_price, 2) AS item_total\n  FROM orders o\n  JOIN customers c\n    ON c.customer_id = o.customer_id\n  JOIN order_items oi\n    ON oi.order_id = o.order_id\n  JOIN products p\n    ON p.product_id = oi.product_id\n WHERE o.order_id >= ${index * 100}\n   AND c.tier IN ('GOLD', 'PLATINUM')\n ORDER BY item_total DESC;`;
    } else if (topic === 'subqueries') {
      code = `SELECT c.customer_id,\n       c.full_name,\n       c.balance,\n       c.tier\n  FROM customers c\n WHERE c.balance >= ${index * 250}.00\n   AND c.customer_id IN (\n       SELECT o.customer_id\n         FROM orders o\n        WHERE o.total_amount > (\n              SELECT AVG(sub.total_amount)\n                FROM orders sub\n               WHERE sub.status = 'COMPLETED'\n              )\n       )\n ORDER BY c.balance DESC;`;
    } else if (topic === 'insert-update-delete') {
      code = `UPDATE employees e\n   SET salary = salary * 1.10,\n       commission_pct = COALESCE(commission_pct, 0.05)\n WHERE e.department_id IN (\n       SELECT d.department_id\n         FROM departments d\n        WHERE d.location_id >= ${1700 + index * 50}\n       )\n   AND e.salary <= (\n       SELECT AVG(inner_e.salary)\n         FROM employees inner_e\n       );`;
    } else {
      // aggregates
      code = `SELECT d.department_name,\n       COUNT(e.employee_id) AS staff_count,\n       ROUND(AVG(e.salary), 2) AS mean_compensation,\n       SUM(e.salary) AS total_payroll,\n       ROUND(MAX(e.salary) - MIN(e.salary), 2) AS salary_disparity\n  FROM departments d\n  JOIN employees e\n    ON e.department_id = d.department_id\n WHERE e.salary >= ${deptMinSal[index - 1]}\n GROUP BY d.department_name\nHAVING COUNT(e.employee_id) >= 2\n ORDER BY total_payroll DESC;`;
    }
  } else {
    // EXPERT
    title = `SQL ${topic.toUpperCase()} Enterprise Query ${index}`;
    noteEn = `Production-scale enterprise analytics report leveraging ${topic}.`;
    noteUzl = `Ishlab chiqarish darajasidagi keng qamrovli korporativ tahliliy hisobot soʻrovi.`;
    noteRu = `Корпоративный аналитический запрос уровня промышленной эксплуатации.`;

    const budgetCeilings = [5000, 6000, 7000, 8000, 9000];
    const tiers = ['GOLD', 'PLATINUM', 'DIAMOND', 'ENTERPRISE', 'VIP'];

    code = `SELECT d.department_name,\n       e.job_id,\n       e.employee_id AS supervisor_id,\n       COUNT(DISTINCT e.employee_id) AS active_headcount,\n       ROUND(AVG(e.salary), 2) AS department_mean_salary,\n       SUM(e.salary) AS gross_salary_budget,\n       ROUND(\n         SUM(e.salary * COALESCE(e.commission_pct, 0.02)),\n         2\n       ) AS estimated_incentive_pool,\n       COUNT(DISTINCT o.order_id) AS attributed_order_volume,\n       COALESCE(SUM(o.total_amount), 0.00) AS merchandise_revenue,\n       ROUND(AVG(COALESCE(pmt.amount, 0.00)), 2) AS average_settlement\n  FROM departments d\n  JOIN employees e\n    ON e.department_id = d.department_id\n  LEFT JOIN customers c\n    ON c.tier = '${tiers[index - 1]}'\n  LEFT JOIN orders o\n    ON o.customer_id = c.customer_id\n   AND o.status = 'COMPLETED'\n  LEFT JOIN payments pmt\n    ON pmt.order_id = o.order_id\n   AND pmt.status = 'SETTLED'\n WHERE d.location_id IS NOT NULL\n   AND e.job_id LIKE '%${topic.slice(0, 3).toUpperCase()}%'\n   AND e.salary >= (\n       SELECT ROUND(AVG(benchmark.salary) * 0.75, 2)\n         FROM employees benchmark\n        WHERE benchmark.department_id = d.department_id\n       )\n GROUP BY d.department_name,\n          e.job_id,\n          e.employee_id\nHAVING COUNT(DISTINCT e.employee_id) >= 1\n   AND SUM(e.salary) > ${budgetCeilings[index - 1]}\n ORDER BY merchandise_revenue DESC,\n          gross_salary_budget DESC,\n          active_headcount ASC;`;
  }

  code = fitCodeToRange(code, diff, '--');

  return {
    language: 'sql',
    topic,
    difficulty: diff,
    index,
    type,
    title,
    noteEn,
    noteUzl,
    noteRu,
    tags: ['sql', topic, diff.toLowerCase()],
    rankedPool: isRanked,
    code,
  };
}
