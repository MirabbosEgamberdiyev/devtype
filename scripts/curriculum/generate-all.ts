import { generateSqlCurriculum } from './generate-sql';
import { generatePlsqlCurriculum } from './generate-plsql';
import { generateJavaCurriculum } from './generate-java';
import { generatePythonCurriculum } from './generate-python';
import { generateJavascriptCurriculum } from './generate-javascript';

console.log('Generating full MVP curriculum (800 snippets across 5 languages)...');

console.log('1. Generating SQL (160 snippets)...');
generateSqlCurriculum();

console.log('2. Generating PL/SQL (160 snippets)...');
generatePlsqlCurriculum();

console.log('3. Generating Java (160 snippets)...');
generateJavaCurriculum();

console.log('4. Generating Python (160 snippets)...');
generatePythonCurriculum();

console.log('5. Generating JavaScript (160 snippets)...');
generateJavascriptCurriculum();

console.log('\nAll 800 curriculum snippets generated successfully.');
