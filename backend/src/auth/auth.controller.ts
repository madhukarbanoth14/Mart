import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { CurrentUser } from './decorators/current-user.decorator';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { AuthService } from './auth.service';
import { ForgotPasswordDto } from './dto/forgot-password.dto';
import { LoginDto } from './dto/login.dto';
import { SendOtpDto, VerifyOtpDto } from './dto/otp.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import {
  SelfRegisterDealerDto,
  SelfRegisterShopkeeperDto,
} from './dto/self-register.dto';
import type { AuthUser } from './types/auth-user.type';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  login(@Body() dto: LoginDto) {
    return this.authService.login(dto);
  }

  @Post('otp/send')
  sendOtp(@Body() dto: SendOtpDto) {
    return this.authService.sendRegisterOtp(dto.phone);
  }

  @Post('otp/verify')
  verifyOtp(@Body() dto: VerifyOtpDto) {
    return this.authService.verifyRegisterOtp(dto.phone, dto.code);
  }

  @Get('registration/areas')
  registrationAreas(
    @Query('state') state?: string,
    @Query('district') district?: string,
  ) {
    return this.authService.registrationAreas(state, district);
  }

  @Get('registration/geo')
  registrationGeo() {
    return this.authService.registrationGeo();
  }

  @Post('register/shopkeeper')
  registerShopkeeper(@Body() dto: SelfRegisterShopkeeperDto) {
    return this.authService.registerShopkeeper(dto);
  }

  @Post('register/dealer')
  registerDealer(@Body() dto: SelfRegisterDealerDto) {
    return this.authService.registerDealer(dto);
  }

  @Post('forgot-password')
  forgotPassword(@Body() dto: ForgotPasswordDto) {
    return this.authService.forgotPassword(dto.email);
  }

  @Post('reset-password')
  resetPassword(@Body() dto: ResetPasswordDto) {
    return this.authService.resetPassword(dto.token, dto.newPassword);
  }

  @Get('me')
  @UseGuards(JwtAuthGuard)
  me(@CurrentUser() user: AuthUser) {
    return this.authService.me(user);
  }
}
