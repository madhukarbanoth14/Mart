"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NestedType = NestedType;
const { Type } = require('class-transformer');
function NestedType(cls) {
    return Type(() => cls);
}
//# sourceMappingURL=nested-type.decorator.js.map