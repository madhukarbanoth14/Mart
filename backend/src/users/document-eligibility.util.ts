import {
  DocumentVerificationStatus,
  UserDocumentStatus,
  UserStatus,
} from '@prisma/client';

export const ORDER_DOCUMENT_BLOCK_MESSAGE =
  'To place orders, please upload at least one valid business document for verification.';

export const DOCUMENT_REJECTED_MESSAGE =
  'Your uploaded document was rejected. Please upload a valid document.';

export const ACCEPTED_DOCUMENT_LABELS: Record<string, string> = {
  AADHAAR: 'Aadhaar Card',
  PAN: 'PAN Card',
  GST: 'GST Certificate',
  TRADE_LICENSE: 'Trade License',
};

type DocRow = { verificationStatus: DocumentVerificationStatus };

export function resolveDocumentTypeFromLabel(
  label: string,
): 'AADHAAR' | 'PAN' | 'GST' | 'TRADE_LICENSE' | null {
  const normalized = label.trim().toLowerCase();
  if (normalized.includes('aadhaar')) return 'AADHAAR';
  if (normalized.includes('pan')) return 'PAN';
  if (normalized.includes('gst')) return 'GST';
  if (normalized.includes('trade') && normalized.includes('license')) {
    return 'TRADE_LICENSE';
  }
  return null;
}

export function computeDocumentEligibility(params: {
  status: UserStatus;
  documents: DocRow[];
}): {
  documentUploaded: boolean;
  documentStatus: UserDocumentStatus;
  canPlaceOrders: boolean;
} {
  const { status, documents } = params;
  if (documents.length === 0) {
    return {
      documentUploaded: false,
      documentStatus: UserDocumentStatus.NOT_UPLOADED,
      canPlaceOrders: false,
    };
  }

  const hasEligible = documents.some(
    (d) =>
      d.verificationStatus === DocumentVerificationStatus.PENDING_VERIFICATION ||
      d.verificationStatus === DocumentVerificationStatus.VERIFIED,
  );

  const allRejected = documents.every(
    (d) => d.verificationStatus === DocumentVerificationStatus.REJECTED,
  );
  const anyVerified = documents.some(
    (d) => d.verificationStatus === DocumentVerificationStatus.VERIFIED,
  );

  let documentStatus: UserDocumentStatus;
  if (allRejected) {
    documentStatus = UserDocumentStatus.REJECTED;
  } else if (anyVerified) {
    documentStatus = UserDocumentStatus.VERIFIED;
  } else {
    documentStatus = UserDocumentStatus.PENDING_VERIFICATION;
  }

  const canPlaceOrders =
    status === UserStatus.ACTIVE && hasEligible;

  return {
    documentUploaded: true,
    documentStatus,
    canPlaceOrders,
  };
}
