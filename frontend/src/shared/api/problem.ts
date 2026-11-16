export interface InvalidParam {
  name: string;
  reason: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  instance?: string;
  code?: string;
  traceId?: string;
  invalidParams?: InvalidParam[];
  [key: string]: unknown;
}

export class ProblemError extends Error {
  readonly status: number;
  readonly code: string;
  readonly traceId?: string;
  readonly invalidParams?: InvalidParam[];
  readonly problem: ProblemDetail;

  constructor(problem: ProblemDetail) {
    super(problem.detail || problem.title || `HTTP Error ${problem.status}`);
    this.name = 'ProblemError';
    this.status = problem.status;
    this.code = problem.code || 'UNKNOWN_ERROR';
    this.traceId = problem.traceId;
    this.invalidParams = problem.invalidParams;
    this.problem = problem;
  }
}
