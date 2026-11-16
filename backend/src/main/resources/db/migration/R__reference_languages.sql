SET lock_timeout = '5s';

INSERT INTO content.languages (slug, name, grammar_id, indent_style, indent_size, keyword_case, token_patterns, special_templates, forbidden_patterns, release, status, sort_order)
VALUES
  ('java', 'Java', 'java', 'SPACE', 4, 'LOWER', '["->", "::", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]", "({type}) {expr}"]}', '[]', 1, 'DRAFT', 1),
  ('python', 'Python', 'python', 'SPACE', 4, 'LOWER', '["->", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 1, 'DRAFT', 2),
  ('javascript', 'JavaScript', 'javascript', 'SPACE', 2, 'LOWER', '["=>", "===", "!==", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 1, 'DRAFT', 3),
  ('sql', 'SQL', 'sql', 'SPACE', 4, 'UPPER', '["<=", ">=", "<>", "!="]', '{"BRACKETS": ["({query})"]}', '["\\$\\$", "LANGUAGE plpgsql", "RAISE NOTICE"]', 1, 'DRAFT', 4),
  ('plsql', 'PL/SQL', 'plsql', 'SPACE', 4, 'UPPER', '[":=", "%TYPE", "%ROWTYPE", "<=", ">=", "<>", "!="]', '{"BRACKETS": ["({params})"]}', '["\\$\\$", "LANGUAGE plpgsql", "RAISE NOTICE", "RETURNS", "PERFORM", "DELIMITER", "DECLARE @"]', 1, 'DRAFT', 5),
  ('typescript', 'TypeScript', 'typescript', 'SPACE', 2, 'LOWER', '["=>", "===", "!==", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 2, 'DRAFT', 6),
  ('csharp', 'C#', 'csharp', 'SPACE', 4, 'LOWER', '["=>", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 2, 'DRAFT', 7),
  ('go', 'Go', 'go', 'TAB', 1, 'LOWER', '[":=", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 2, 'DRAFT', 8),
  ('kotlin', 'Kotlin', 'kotlin', 'SPACE', 4, 'LOWER', '["->", "==", "!=", "===", "!=="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 2, 'DRAFT', 9),
  ('cpp', 'C++', 'cpp', 'SPACE', 4, 'LOWER', '["->", "::", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 2, 'DRAFT', 10),
  ('bash', 'Bash', 'bash', 'SPACE', 2, 'LOWER', '["==", "!=", "-eq", "-ne"]', '{"BRACKETS": ["${ident}"]}', '[]', 2, 'DRAFT', 11),
  ('c', 'C', 'c', 'SPACE', 4, 'LOWER', '["->", "==", "!=", "<=", ">="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 3, 'DRAFT', 12),
  ('php', 'PHP', 'php', 'SPACE', 4, 'LOWER', '["->", "=>", "===", "!=="]', '{"BRACKETS": ["${ident}"]}', '[]', 3, 'DRAFT', 13),
  ('rust', 'Rust', 'rust', 'SPACE', 4, 'LOWER', '["->", "=>", "::", "==", "!="]', '{"BRACKETS": ["{ident}[{n}]"]}', '[]', 3, 'DRAFT', 14),
  ('html', 'HTML', 'html', 'SPACE', 2, 'LOWER', '["</", "/>", "=\""]', '{"BRACKETS": ["<{tag}>"]}', '[]', 3, 'DRAFT', 15),
  ('css', 'CSS', 'css', 'SPACE', 2, 'LOWER', '[": ", ";", "{", "}"]', '{"BRACKETS": ["{prop}: {val};"]}', '[]', 3, 'DRAFT', 16)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  grammar_id = EXCLUDED.grammar_id,
  indent_style = EXCLUDED.indent_style,
  indent_size = EXCLUDED.indent_size,
  keyword_case = EXCLUDED.keyword_case,
  token_patterns = EXCLUDED.token_patterns,
  special_templates = EXCLUDED.special_templates,
  forbidden_patterns = EXCLUDED.forbidden_patterns,
  release = EXCLUDED.release,
  sort_order = EXCLUDED.sort_order;
