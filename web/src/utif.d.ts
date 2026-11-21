declare module 'utif' {
  interface IFD {
    width: number;
    height: number;
    data: Uint8Array;
    t258?: number[];
    t262?: number[];
    [tag: string]: unknown;
  }
  const UTIF: {
    decode(buf: ArrayBuffer): IFD[];
    decodeImage(buf: ArrayBuffer, ifd: IFD): void;
    toRGBA8(ifd: IFD): Uint8Array;
  };
  export default UTIF;
}
