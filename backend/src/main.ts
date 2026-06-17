import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

process.on('warning', (warning) => {
  const msg = warning?.message ?? '';
  if (
    warning.name === 'DeprecationWarning' &&
    msg.includes(
      'Calling client.query() when the client is already executing a query is deprecated',
    )
  ) {
    return;
  }
  // Preserve all other warnings for visibility.
  // eslint-disable-next-line no-console
  console.warn(warning);
});

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Allow browser clients (e.g. the Flashmart web UI served on another port)
  // to call the API. CORS_ORIGINS is a comma-separated allow-list; when unset
  // we reflect the request origin, which is convenient for local development.
  const corsOrigins = (process.env.CORS_ORIGINS ?? '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean);
  app.enableCors({
    origin: corsOrigins.length > 0 ? corsOrigins : true,
    credentials: true,
  });

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
    }),
  );

  // Cloud Run sets PORT (8080). Local dev typically omits it — default 3000.
  const port = Number.parseInt(process.env.PORT ?? '3000', 10);
  await app.listen(port, '0.0.0.0');
}
void bootstrap();
