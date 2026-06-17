import { UserRole, UserStatus } from '@prisma/client';
export declare class ListUsersQueryDto {
    role?: UserRole;
    status?: UserStatus;
}
