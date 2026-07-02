import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { normalizeAreaName } from './area-name.util';
import { CreateAreaDto } from './dto/create-area.dto';
import { UpdateAreaDto } from './dto/update-area.dto';

@Injectable()
export class AreasService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser) {
    const where: Prisma.AreaWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.DEALER) {
      where.dealerId = actor.userId;
    }

    return this.prisma.area.findMany({
      where,
      include: { dealer: { select: { id: true, name: true, email: true } } },
      orderBy: { name: 'asc' },
    });
  }

  async create(actor: AuthUser, dto: CreateAreaDto) {
    if (actor.role !== UserRole.ADMIN) {
      throw new ForbiddenException('Only admins can add areas');
    }
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required to add areas');
    }
    const name = normalizeAreaName(dto.name);
    const existing = await this.prisma.area.findFirst({
      where: {
        companyId: actor.companyId,
        name: { equals: name, mode: 'insensitive' },
      },
    });
    if (existing) {
      throw new ConflictException('An area with this name already exists');
    }
    return this.prisma.area.create({
      data: { name, companyId: actor.companyId },
      include: { dealer: { select: { id: true, name: true, email: true } } },
    });
  }

  async update(actor: AuthUser, areaId: string, dto: UpdateAreaDto) {
    if (actor.role !== UserRole.ADMIN) {
      throw new ForbiddenException('Only admins can rename areas');
    }
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required to update areas');
    }
    const existing = await this.prisma.area.findFirst({
      where: { id: areaId, companyId: actor.companyId },
    });
    if (!existing) {
      throw new NotFoundException('Area not found');
    }
    const name = normalizeAreaName(dto.name);
    const duplicate = await this.prisma.area.findFirst({
      where: {
        companyId: actor.companyId,
        id: { not: areaId },
        name: { equals: name, mode: 'insensitive' },
      },
    });
    if (duplicate) {
      throw new ConflictException('An area with this name already exists');
    }
    return this.prisma.area.update({
      where: { id: areaId },
      data: { name },
      include: { dealer: { select: { id: true, name: true, email: true } } },
    });
  }

  private async defaultCompanyId(): Promise<string | null> {
    const fromEnv = process.env.MART_COMPANY_ID?.trim();
    if (fromEnv) return fromEnv;
    const company = await this.prisma.company.findFirst({
      orderBy: { createdAt: 'asc' },
      select: { id: true },
    });
    return company?.id ?? null;
  }

  /** Ensure at least one company exists for open registration (empty local DB). */
  private async ensureDefaultCompanyId(): Promise<string> {
    const existing = await this.defaultCompanyId();
    if (existing) return existing;
    const company = await this.prisma.company.create({
      data: { name: 'FlashMart Distribution' },
      select: { id: true },
    });
    return company.id;
  }

  /**
   * Registration geo lists districts even when the Area table is empty (local dev).
   * Create default service areas so shopkeepers can complete signup.
   */
  private async ensureRegistrationAreas(companyId: string): Promise<void> {
    const count = await this.prisma.area.count({ where: { companyId } });
    if (count > 0) return;

    for (const area of DEFAULT_REGISTRATION_AREAS) {
      await this.prisma.area.create({
        data: {
          name: area.name,
          state: area.state,
          district: area.district,
          companyId,
        },
      });
    }
  }

  async findForRegistration(state?: string, district?: string) {
    const companyId = await this.ensureDefaultCompanyId();
    await this.ensureRegistrationAreas(companyId);
    const baseWhere: Prisma.AreaWhereInput = { companyId };

    const withGeo: Prisma.AreaWhereInput = { ...baseWhere };
    const stateTrim = state?.trim();
    const districtTrim = district?.trim();
    if (stateTrim) {
      withGeo.state = { equals: stateTrim, mode: 'insensitive' };
    }
    if (districtTrim && !isAllDistrictsLabel(districtTrim)) {
      withGeo.district = { equals: districtTrim, mode: 'insensitive' };
    }

    const select = {
      id: true,
      name: true,
      state: true,
      district: true,
      dealerId: true,
    } as const;

    let areas = await this.prisma.area.findMany({
      where: withGeo,
      select,
      orderBy: { name: 'asc' },
    });

    // Areas may not have state/district populated yet — return company areas instead of an empty list.
    if (areas.length === 0 && (stateTrim || districtTrim)) {
      areas = await this.prisma.area.findMany({
        where: baseWhere,
        select,
        orderBy: { name: 'asc' },
      });
    }

    return areas;
  }

  async listRegistrationGeo() {
    const companyId = await this.ensureDefaultCompanyId();
    await this.ensureRegistrationAreas(companyId);
    const areas = await this.prisma.area.findMany({
      where: { companyId },
      select: { state: true, district: true },
    });
    const states = new Map<string, Set<string>>();
    for (const area of areas) {
      const state = area.state?.trim() || 'Telangana';
      const district = area.district?.trim() || ALL_DISTRICTS_LABEL;
      if (!states.has(state)) states.set(state, new Set());
      states.get(state)!.add(district);
    }
    if (states.size === 0) {
      return {
        states: [
          {
            name: 'Telangana',
            districts: ['Warangal', 'Hanamkonda', 'Jangaon'],
          },
        ],
      };
    }
    return {
      states: [...states.entries()].map(([name, districts]) => ({
        name,
        districts: [...districts].sort(),
      })),
    };
  }
}

const ALL_DISTRICTS_LABEL = 'All areas';

const DEFAULT_REGISTRATION_AREAS = [
  { name: 'Warangal', state: 'Telangana', district: 'Warangal' },
  { name: 'Hanamkonda', state: 'Telangana', district: 'Hanamkonda' },
  { name: 'Jangaon', state: 'Telangana', district: 'Jangaon' },
] as const;

function isAllDistrictsLabel(district: string): boolean {
  const d = district.trim().toLowerCase();
  return d === ALL_DISTRICTS_LABEL.toLowerCase() || d === 'other';
}
