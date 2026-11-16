import { writeSnippet, fitCodeToRange } from '../generate-curriculum-800';

export function generatePlsqlCurriculum(): void {
  const topics = [
    'fundamentals',
    'block-structure',
    'variables',
    'conditions',
    'loops',
    'procedure',
    'function',
    'cursor',
  ];

  for (const topic of topics) {
    for (const diff of ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as const) {
      for (let i = 1; i <= 5; i++) {
        const snippet = createPlsqlSnippet(topic, diff, i);
        writeSnippet(snippet);
      }
    }
  }
}

function createPlsqlSnippet(
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
  const cleanTopic = topic.replace(/-/g, '_');

  if (['fundamentals', 'block-structure', 'variables', 'conditions'].includes(topic) && diff === 'BEGINNER') {
    type = 'SPECIAL_CHARS';
  }

  if (diff === 'BEGINNER') {
    title = `Oracle PL/SQL ${topic.toUpperCase()} Drill ${index}`;
    noteEn = `Beginner drill for Oracle PL/SQL ${topic}.`;
    noteUzl = `Oracle PL/SQL ${topic} boʻyicha boshlangʻich amaliy mashq.`;
    noteRu = `Базовое упражнение по Oracle PL/SQL ${topic}.`;

    if (topic === 'fundamentals') {
      const msgs = ['Hello, DevType!', 'Oracle PL/SQL Engine', 'Active Session', 'Compiler Ready', 'Production Unit'];
      code = `DECLARE\n  v_greeting VARCHAR2(50) := '${msgs[index - 1]}';\nBEGIN\n  DBMS_OUTPUT.PUT_LINE(v_greeting || ' ' || ${index});\nEND;\n/`;
    } else if (topic === 'block-structure') {
      const offsets = [25, 50, 75, 100, 125];
      code = `DECLARE\n  v_total NUMBER := 100 * ${index};\nBEGIN\n  v_total := v_total + ${offsets[index - 1]};\n  DBMS_OUTPUT.PUT_LINE('Result: ' || v_total);\nEND;\n/`;
    } else if (topic === 'variables') {
      const empIds = [101, 102, 103, 104, 105];
      const sals = [4200.00, 4500.00, 4800.00, 5100.00, 5400.00];
      code = `DECLARE\n  v_emp_id   employees.employee_id%TYPE := ${empIds[index - 1]};\n  v_salary   NUMBER(8, 2) := ${sals[index - 1].toFixed(2)};\nBEGIN\n  DBMS_OUTPUT.PUT_LINE(v_emp_id || ': ' || v_salary);\nEND;\n/`;
    } else if (topic === 'conditions') {
      const thresholds = [70, 75, 80, 85, 90];
      code = `DECLARE\n  v_score NUMBER := 75 + ${index};\nBEGIN\n  IF v_score >= ${thresholds[index - 1]} THEN\n    DBMS_OUTPUT.PUT_LINE('Status: PASSED');\n  ELSE\n    DBMS_OUTPUT.PUT_LINE('Status: RETRY');\n  END IF;\nEND;\n/`;
    } else if (topic === 'loops') {
      const maxLoops = [3, 4, 5, 6, 7];
      code = `DECLARE\n  v_idx NUMBER := 1;\nBEGIN\n  WHILE v_idx <= ${maxLoops[index - 1]} LOOP\n    DBMS_OUTPUT.PUT_LINE('Loop count: ' || v_idx);\n    v_idx := v_idx + 1;\n  END LOOP;\nEND;\n/`;
    } else if (topic === 'procedure') {
      code = `CREATE OR REPLACE PROCEDURE log_audit_${index} (\n  p_msg IN VARCHAR2\n) IS\nBEGIN\n  DBMS_OUTPUT.PUT_LINE('AUDIT [${index}]: ' || p_msg);\nEND log_audit_${index};\n/`;
    } else if (topic === 'function') {
      const multipliers = [0.10, 0.15, 0.20, 0.25, 0.30];
      code = `CREATE OR REPLACE FUNCTION get_rate_${index} (\n  p_base IN NUMBER\n) RETURN NUMBER IS\nBEGIN\n  RETURN p_base * ${multipliers[index - 1].toFixed(2)};\nEND get_rate_${index};\n/`;
    } else {
      // cursor
      const empIds = [201, 202, 203, 204, 205];
      code = `DECLARE\n  v_name VARCHAR2(100);\nBEGIN\n  SELECT first_name\n    INTO v_name\n    FROM employees\n   WHERE employee_id = ${empIds[index - 1]};\n  DBMS_OUTPUT.PUT_LINE(v_name);\nEND;\n/`;
    }
  } else if (diff === 'INTERMEDIATE') {
    title = `Oracle PL/SQL ${topic.toUpperCase()} Intermediate Block ${index}`;
    noteEn = `Intermediate Oracle PL/SQL block focusing on ${topic}.`;
    noteUzl = `Oracle PL/SQL da ${topic} boʻyicha oʻrta murakkablikdagi dasturiy blok.`;
    noteRu = `Промежуточный программный блок Oracle PL/SQL по теме ${topic}.`;

    if (topic === 'procedure') {
      const rates = [0.04, 0.05, 0.06, 0.07, 0.08];
      code = `CREATE OR REPLACE PROCEDURE update_bonus_${index} (\n  p_employee_id IN employees.employee_id%TYPE,\n  p_bonus_rate  IN NUMBER DEFAULT ${rates[index - 1].toFixed(2)}\n) IS\n  v_old_salary employees.salary%TYPE;\nBEGIN\n  SELECT salary\n    INTO v_old_salary\n    FROM employees\n   WHERE employee_id = p_employee_id\n     FOR UPDATE;\n\n  UPDATE employees\n     SET salary = v_old_salary * (1 + p_bonus_rate)\n   WHERE employee_id = p_employee_id;\n\n  COMMIT;\nEND update_bonus_${index};\n/`;
    } else if (topic === 'function') {
      const multipliers = [1.05, 1.08, 1.10, 1.12, 1.15];
      code = `CREATE OR REPLACE FUNCTION calc_future_sal_${index} (\n  p_emp_id IN employees.employee_id%TYPE\n) RETURN NUMBER IS\n  v_curr_salary employees.salary%TYPE;\nBEGIN\n  SELECT salary\n    INTO v_curr_salary\n    FROM employees\n   WHERE employee_id = p_emp_id;\n\n  RETURN ROUND(v_curr_salary * ${multipliers[index - 1].toFixed(2)}, 2);\nEND calc_future_sal_${index};\n/`;
    } else if (topic === 'cursor') {
      const locs = [1700, 1800, 1900, 2000, 2100];
      code = `DECLARE\n  CURSOR c_dept_${index} IS\n    SELECT department_id, department_name\n      FROM departments\n     WHERE location_id = ${locs[index - 1]};\n  v_id   departments.department_id%TYPE;\n  v_name departments.department_name%TYPE;\nBEGIN\n  OPEN c_dept_${index};\n  LOOP\n    FETCH c_dept_${index} INTO v_id, v_name;\n    EXIT WHEN c_dept_${index}%NOTFOUND;\n    DBMS_OUTPUT.PUT_LINE(v_id || ': ' || v_name);\n  END LOOP;\n  CLOSE c_dept_${index};\nEND;\n/`;
    } else {
      const depts = [10, 20, 30, 40, 50];
      const minEmps = [1, 2, 3, 4, 5];
      code = `DECLARE\n  v_dept_id departments.department_id%TYPE := ${depts[index - 1]};\n  v_avg_sal NUMBER(10, 2);\n  v_emp_cnt PLS_INTEGER;\nBEGIN\n  SELECT COUNT(*), ROUND(AVG(salary), 2)\n    INTO v_emp_cnt, v_avg_sal\n    FROM employees\n   WHERE department_id = v_dept_id;\n\n  IF v_emp_cnt >= ${minEmps[index - 1]} THEN\n    DBMS_OUTPUT.PUT_LINE('${cleanTopic}_${index}: ' || v_avg_sal);\n  END IF;\nEND;\n/`;
    }
  } else if (diff === 'ADVANCED') {
    title = `Oracle PL/SQL ${topic.toUpperCase()} Advanced Subprogram ${index}`;
    noteEn = `Advanced Oracle PL/SQL subprogram demonstrating robust exception handling.`;
    noteUzl = `Mustahkam xatoliklarni qayta ishlashga ega murakkab PL/SQL dasturi.`;
    noteRu = `Продвинутая подпрограмма Oracle PL/SQL с обработкой исключительных ситуаций.`;

    const maxPcts = [30, 35, 40, 45, 50];
    const deptIds = [20, 30, 40, 50, 60];
    code = `CREATE OR REPLACE PROCEDURE exec_${cleanTopic}_hike_${index} (\n  p_department_id IN departments.department_id%TYPE,\n  p_percentage    IN NUMBER\n) IS\n  v_updated_rows PLS_INTEGER := 0;\n  e_invalid_rate EXCEPTION;\nBEGIN\n  IF p_percentage <= 0 OR p_percentage > ${maxPcts[index - 1]} THEN\n    RAISE e_invalid_rate;\n  END IF;\n\n  UPDATE employees\n     SET salary = salary * (1 + p_percentage / 100)\n   WHERE department_id = p_department_id;\n\n  v_updated_rows := SQL%ROWCOUNT;\n  DBMS_OUTPUT.PUT_LINE('${cleanTopic} rows: ' || v_updated_rows);\n  COMMIT;\nEXCEPTION\n  WHEN e_invalid_rate THEN\n    ROLLBACK;\n    RAISE_APPLICATION_ERROR(\n      -20010,\n      'Percentage increment out of acceptable range'\n    );\n  WHEN NO_DATA_FOUND THEN\n    ROLLBACK;\n    RAISE_APPLICATION_ERROR(\n      -20011,\n      'Department ID does not exist: ' || p_department_id\n    );\n  WHEN OTHERS THEN\n    ROLLBACK;\n    RAISE_APPLICATION_ERROR(\n      -20099,\n      'Unexpected exception in payroll routine'\n    );\nEND exec_${cleanTopic}_hike_${index};\n/`;
  } else {
    // EXPERT
    title = `Oracle PL/SQL ${topic.toUpperCase()} Enterprise Package ${index}`;
    noteEn = `Enterprise-grade production Oracle PL/SQL package implementation.`;
    noteUzl = `Ishlab chiqarish talablariga mos toʻliq korporativ Oracle PL/SQL paketi.`;
    noteRu = `Корпоративный пакет Oracle PL/SQL промышленного уровня.`;

    const bonusCaps = [4000.00, 5000.00, 6000.00, 7000.00, 8000.00];
    code = `CREATE OR REPLACE PACKAGE BODY ${cleanTopic}_engine_pkg_${index} IS\n  c_max_bonus CONSTANT NUMBER := ${bonusCaps[index - 1].toFixed(2)};\n\n  PROCEDURE calculate_compensation (\n    p_emp_id       IN employees.employee_id%TYPE,\n    p_perf_score   IN NUMBER,\n    p_final_payout OUT NUMBER\n  ) IS\n    v_base_salary employees.salary%TYPE;\n    v_incentive   NUMBER := 0.0;\n    e_zero_score  EXCEPTION;\n  BEGIN\n    IF p_perf_score <= 0.0 THEN\n      RAISE e_zero_score;\n    END IF;\n\n    SELECT salary\n      INTO v_base_salary\n      FROM employees\n     WHERE employee_id = p_emp_id;\n\n    v_incentive := LEAST(\n      v_base_salary * (p_perf_score / 100),\n      c_max_bonus\n    );\n    p_final_payout := v_base_salary + v_incentive;\n\n    INSERT INTO audit_log (\n      entity_name, entity_id, action, performed_by\n    )\n    VALUES ('${cleanTopic.toUpperCase()}', p_emp_id, 'CALCULATE', 'SYSTEM');\n    COMMIT;\n  EXCEPTION\n    WHEN e_zero_score THEN\n      p_final_payout := 0.0;\n      RAISE_APPLICATION_ERROR(\n        -20050,\n        'Performance evaluation score must be positive'\n      );\n    WHEN NO_DATA_FOUND THEN\n      RAISE_APPLICATION_ERROR(\n        -20051,\n        'Employee profile not registered: ' || p_emp_id\n      );\n    WHEN OTHERS THEN\n      ROLLBACK;\n      RAISE_APPLICATION_ERROR(\n        -20052,\n        'Critical error executing payroll batch job'\n      );\n  END calculate_compensation;\nEND ${cleanTopic}_engine_pkg_${index};\n/`;
  }

  code = fitCodeToRange(code, diff, '--');

  return {
    language: 'plsql',
    topic,
    difficulty: diff,
    index,
    type,
    title,
    noteEn,
    noteUzl,
    noteRu,
    tags: ['plsql', topic, diff.toLowerCase()],
    rankedPool: isRanked,
    code,
  };
}
