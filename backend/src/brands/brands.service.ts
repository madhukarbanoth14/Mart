import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBrandDto } from './dto/create-brand.dto';
import { UpdateBrandDto } from './dto/update-brand.dto';

@Injectable()
export class BrandsService {
  constructor(private readonly prisma: PrismaService) {}

  findAll() {
    return this.prisma.brand.findMany({
      select: {
        id: true,
        name: true,
        logoUrl: true,
        manufacturer: true,
      },
      orderBy: { name: 'asc' },
    });
  }

  async findOne(id: string) {
    const brand = await this.prisma.brand.findUnique({
      where: { id },
      select: {
        id: true,
        name: true,
        logoUrl: true,
        manufacturer: true,
        category: true,
        companyId: true,
      },
    });
    if (!brand) {
      throw new NotFoundException('Brand not found');
    }
    return brand;
  }

  async create(actor: AuthUser, dto: CreateBrandDto) {
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required to create brands');
    }
    try {
      return await this.prisma.brand.create({
        data: {
          name: dto.name.trim(),
          logoUrl: dto.logoUrl?.trim() || null,
          companyId: actor.companyId,
        },
      });
    } catch (error) {
      if (
        error instanceof Prisma.PrismaClientKnownRequestError &&
        error.code === 'P2002'
      ) {
        throw new BadRequestException('Brand name already exists');
      }
      throw error;
    }
  }

  async update(id: string, actor: AuthUser, dto: UpdateBrandDto) {
    const existing = await this.prisma.brand.findUnique({ where: { id } });
    if (!existing) {
      throw new NotFoundException('Brand not found');
    }
    if (actor.companyId && existing.companyId !== actor.companyId) {
      throw new BadRequestException('Brand does not belong to your company');
    }
    try {
      return await this.prisma.brand.update({
        where: { id },
        data: {
          name: dto.name?.trim(),
          logoUrl: dto.logoUrl?.trim() || null,
        },
      });
    } catch (error) {
      if (
        error instanceof Prisma.PrismaClientKnownRequestError &&
        error.code === 'P2002'
      ) {
        throw new BadRequestException('Brand name already exists');
      }
      throw error;
    }
  }

  async remove(id: string, actor: AuthUser) {
    const existing = await this.prisma.brand.findUnique({ where: { id } });
    if (!existing) {
      throw new NotFoundException('Brand not found');
    }
    if (actor.companyId && existing.companyId !== actor.companyId) {
      throw new BadRequestException('Brand does not belong to your company');
    }
    return this.prisma.brand.delete({ where: { id } });
  }
}
