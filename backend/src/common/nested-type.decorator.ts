/**
 * class-transformer's `Type` decorator exists at runtime but is missing from typings when
 * `moduleResolution: "nodenext"` resolves the package's incomplete `.d.ts` chain.
 */
// eslint-disable-next-line @typescript-eslint/no-require-imports
const { Type } = require('class-transformer') as {
  Type: (fn: () => new (...args: never[]) => unknown) => PropertyDecorator;
};

export function NestedType<T>(cls: new () => T): PropertyDecorator {
  return Type(() => cls);
}
