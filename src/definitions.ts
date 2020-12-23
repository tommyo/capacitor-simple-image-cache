declare module '@capacitor/core' {
  interface PluginRegistry {
    ImageCache: ImageCachePlugin;
  }
}

export interface Result<T = boolean> {
  value: T;
}

export interface Options {
  src: string;
}

export interface ImageCachePlugin {
  get(options: Options & { overwrite?: boolean }): Promise<Result<string>>;
  hasItem(options: Options):Promise<Result>;
  clearItem(options: Options):Promise<Result>;
  clear():Promise<Result>;
}
