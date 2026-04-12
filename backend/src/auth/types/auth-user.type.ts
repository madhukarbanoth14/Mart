import { UserRole } from '@prisma/client';

export type AuthUser = {
  userId: string;
  email: string;
  role: UserRole;
  companyId: string | null;
};
