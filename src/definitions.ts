export interface CapacitorXprinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
