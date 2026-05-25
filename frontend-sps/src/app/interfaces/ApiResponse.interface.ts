export interface ApiResponse<T> {
  status: 'OK' | 'ERROR';
  data: T;
  message: string;
  timestamp: string;
}
