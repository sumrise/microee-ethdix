import express from 'express';
import debug from 'debug';
import { AssertionError } from 'chai';

const loggerError: debug.IDebugger = debug('app-error');

export class ErrorHandler extends Error {
  code: number;
  message: string;
  constructor(code: number, message: string) {
    super();
    this.code = code;
    this.message = message;
  }
  static handle(errCode: number, err: Error, res: express.Response) {
    if (err instanceof AssertionError) {
      const code = 413;
      const { message } = err;
      res.status(200).json({ code: code, message: `[uniswap-dixx](${message.split(':')[0]})` });
      return;
    }
    if (err instanceof ErrorHandler) {
      const { code, message } = err;
      loggerError(`errorCode=${code}, errorMessage=${message}`);
      res.status(200).json({ code: code, message: `[uniswap-dixx](${message})` });
      return;
    }
    loggerError(`type=${typeof err}, errorCode=${errCode}, errorMessage=${err.message || err.stack?.toString().split('\n')[0]}, stack=${err.stack}`);
    res.status(errCode).json({ code: errCode, message: `[uniswap-dixx](${err.message || err.stack?.toString().split('\n')[0]})` });
  }
}
