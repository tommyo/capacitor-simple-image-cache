import { WebPlugin } from '@capacitor/core';
import { ImageCachePlugin, Options, Result } from './definitions';

const cacheName = '__capacitor_simple_image_cache__';

export class ImageCacheWeb extends WebPlugin implements ImageCachePlugin {
  // cache: Promise<Cache | null>;
  
  constructor() {
    super({
      name: 'ImageCache',
      platforms: ['web'],
    });

    console.log({ cacheName });
    // this.cache = caches?.open(cacheName);
    // this.cache = Promise.resolve(null);

    // inject the listener to check the cache on image fetch
    // self.addEventListener('fetch', async (event) => {
    //   console.log('CACHE CHECK', { event })
    // });
  }
  async get({ src }: Options & { overwrite?: boolean | undefined; }): Promise<Result<string>> {
    console.log({ src });
    return { value: src };
    // const cache = await this.cache;
    // const match = await cache.match(src);
    // if (!match || overwrite) {
    //   await cache.add(src);
    // }
    // return { value: `imagecache+${src}` };
  }
  async hasItem({ src }: Options): Promise<Result<boolean>> {
    console.log({ src });
    // const cache = await this.cache;
    // const match = await cache?.match(src);
    // return { value: !!match };
    return { value: false };
  }
  async clearItem({ src }: Options): Promise<Result<boolean>> {
    console.log({ src }); 
    // const cache = await this.cache;
    // const match = await cache?.match(src);
    // if (match) {
    //   await cache?.delete(src);
    // }
    return { value: true };
  }
  async clear(): Promise<Result<boolean>> {
    // await caches.delete(cacheName);
    // // reopen
    // this.cache = caches.open(cacheName);
    return { value: true };
  }
  
}

const ImageCache = new ImageCacheWeb();

export { ImageCache };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ImageCache);
