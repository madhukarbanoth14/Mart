import { IsIn, IsOptional, IsString } from 'class-validator';

export class VerifyDocumentDto {
  @IsOptional()
  @IsString()
  notes?: string;
}

export class RejectDocumentDto {
  @IsString()
  reason!: string;
}

export class RecordFollowUpDto {
  @IsOptional()
  @IsString()
  notes?: string;
}

export class UploadMyDocumentDto {
  @IsIn(['AADHAAR', 'PAN', 'GST', 'TRADE_LICENSE'])
  documentType!: 'AADHAAR' | 'PAN' | 'GST' | 'TRADE_LICENSE';
}
