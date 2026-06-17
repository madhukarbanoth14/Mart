"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.DealerAssignmentsModule = void 0;
const common_1 = require("@nestjs/common");
const areas_module_1 = require("../areas/areas.module");
const roles_module_1 = require("../roles/roles.module");
const dealer_assignments_controller_1 = require("./dealer-assignments.controller");
const dealer_assignments_service_1 = require("./dealer-assignments.service");
let DealerAssignmentsModule = class DealerAssignmentsModule {
};
exports.DealerAssignmentsModule = DealerAssignmentsModule;
exports.DealerAssignmentsModule = DealerAssignmentsModule = __decorate([
    (0, common_1.Module)({
        imports: [areas_module_1.AreasModule, roles_module_1.RolesModule],
        controllers: [dealer_assignments_controller_1.DealerAssignmentsController],
        providers: [dealer_assignments_service_1.DealerAssignmentsService],
        exports: [dealer_assignments_service_1.DealerAssignmentsService],
    })
], DealerAssignmentsModule);
//# sourceMappingURL=dealer-assignments.module.js.map