import { Injectable } from '@nestjs/common';
import { Prisma, UserRole } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class InvoicesService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(actor: AuthUser) {
    const where: Prisma.InvoiceWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (actor.role === UserRole.SHOPKEEPER) {
      where.order = { shopkeeperId: actor.userId };
    } else if (actor.role === UserRole.DEALER) {
      where.order = { dealerId: actor.userId };
    }

    return this.prisma.invoice.findMany({
      where,
      include: {
        order: {
          include: {
            items: { include: { product: true } },
            shopkeeper: { select: { id: true, name: true } },
            dealer: { select: { id: true, name: true } },
          },
        },
      },
      orderBy: { generatedAt: 'desc' },
    });
  }

  /**
   * Builds a plain object suitable for PDF rendering later (no file I/O in demo).
   */
  async getInvoiceDocument(orderId: string) {
    const invoice = await this.prisma.invoice.findUnique({
      where: { orderId },
      include: {
        order: {
          include: {
            items: { include: { product: true } },
            shopkeeper: { select: { id: true, name: true, email: true } },
            dealer: { select: { id: true, name: true, email: true } },
          },
        },
      },
    });
    if (!invoice) {
      return null;
    }
    return {
      invoiceNumber: invoice.invoiceNumber,
      generatedAt: invoice.generatedAt,
      pdfUrl: invoice.pdfUrl,
      order: invoice.order,
    };
  }

  async generateForOrder(orderId: string, companyId?: string | null) {
    const invoiceNumber = `INV-${orderId.replace(/-/g, '').toUpperCase()}`;

    return this.prisma.invoice.upsert({
      where: { orderId },
      create: {
        orderId,
        companyId: companyId ?? null,
        invoiceNumber,
      },
      update: {},
    });
  }
}
