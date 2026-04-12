import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

/**
 * Encapsulates persistence and calculations for line items (used by OrdersService in Step 4).
 */
@Injectable()
export class OrderItemsService {
  constructor(private readonly prisma: PrismaService) {}

  findByOrderId(orderId: string) {
    return this.prisma.orderItem.findMany({
      where: { orderId },
      include: { product: true },
    });
  }
}
