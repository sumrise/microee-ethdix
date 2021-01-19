import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import { ChainId, Route, Token, TokenAmount, TradeType, Trade, WETH, Percent, JSBI, CurrencyAmount } from '@uniswap/sdk';
import { ethers } from 'ethers';
import { parseUnits } from '@ethersproject/units';
import web3 from 'web3';

import { getTokenDataByAddr, getPairDataWithSymbol, getTokenObjectByAddress } from '../../univ2-resolves/UniV2Fetcher';

const loggerInfo: debug.IDebugger = debug('app-univ2-trade');

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2TradeRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2TradeRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-trade`)
            .get((req: express.Request, res: express.Response) => {
                res.status(200).send(`UniSwapV2 Trading Page.`);
            });
        // 根据输入的数量返回代币的最小单位表示形式
        this.app.route(`/univ2-trade/jsbiBigInt`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _amount: string = req.query['amount'] as string; // 代币数量
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                expect(_amount, '_amount invalid').to.length.gt(0);
                (async () => {
                    try {
                        const _token: Token = await getTokenDataByAddr(_chainId, _tokenAddr);
                        const typedValueParsed = parseUnits(_amount, _token.decimals).toString();
                        expect(typedValueParsed !== '0', 'amount must > 0').to.be.true;
                        const _currencyAmount = new TokenAmount(_token, JSBI.BigInt(typedValueParsed));
                        res.status(200).json({ code: 200, message: _currencyAmount.toSignificant(6), data: typedValueParsed });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 获取 UniSwapV2 兑换参数数据
        this.app.route(`/univ2-trade/eth2TokenSwapGetParams`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _ethInputAmount: string = req.body.ethInputAmount as string;
                const _slippageToleranceInput: number[] = req.body.slippageTolerance as number[]; // new Percent('50', '10000') // 50 bips 1 bip = 0.05
                expect(web3.utils.isAddress(_tokenAddr), '_tokenAddr not invalid').to.be.true;
                expect(_ethInputAmount, 'ethInputAmount shoud not be empty').to.length.gt(0);
                expect(_ethInputAmount, 'ethInputAmount shoud not equals 0').to.not.equal('0');
                expect(_slippageToleranceInput, 'slippageTolerance shoud length of 2').to.have.lengthOf(2);
                expect(_slippageToleranceInput[0], 'slippageTolerance[0] not invalid').to.be.gt(1);
                expect(_slippageToleranceInput[1], 'slippageTolerance[1] shoud be > slippageTolerance[0]').to.be.gt(_slippageToleranceInput[0]);
                const _wethAddr = WETH[_chainId];
                (async () => {
                    try {
                        const token = await getTokenDataByAddr(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                        const pairWithSymbol = await getPairDataWithSymbol(_chainId, token, _wethAddr);
                        const pair = pairWithSymbol[0];
                        const route = new Route([pair], _wethAddr);
                        const _tokenAmountString = parseUnits(_ethInputAmount, _wethAddr.decimals).toString();
                        const trade = new Trade(route, new TokenAmount(_wethAddr, _tokenAmountString), TradeType.EXACT_INPUT);
                        const _slippageTolerance = new Percent(_slippageToleranceInput[0].toString(), _slippageToleranceInput[1].toString()); 
                        const tradInfo = {
                            slippageTolerance: _slippageTolerance.toSignificant(6),
                            amountOutMin: trade.minimumAmountOut(_slippageTolerance).toSignificant(6),
                            path: [_wethAddr.address, token.address],
                            to: '',
                            deadline: Math.floor(new Date().getTime() / 1000) + 60 * 20, // 20 分钟
                            value: trade.inputAmount.toSignificant(6)
                        };
                        const message = `兑换-ETH2TokenSwap: 
                                        chainId=${_chainId},
                                        slippageTolerance=${tradInfo.slippageTolerance}, 
                                        amountOutMin=${tradInfo.amountOutMin}, 
                                        path:[${route.path[0].symbol || route.path[0].address}, ${route.path[1].symbol || route.path[1].address}],
                                        to: ${tradInfo.to},
                                        deadline: ${tradInfo.deadline},
                                        value: ${tradInfo.value},
                                        value2: ${_tokenAmountString},
                                        liquidityToken=${pair.liquidityToken.symbol}, 
                                        route.midPrice=${route.midPrice.toSignificant(6)}, 
                                        route.midPrice.invert=${route.midPrice.invert().toSignificant(6)},
                                        trade.executionPrice=${trade.executionPrice.toSignificant(6)},
                                        trade.nextMidPrice=${trade.nextMidPrice.toSignificant(6)}`.replace(/(\r\n|\n|\r)/gm, "").replace(/(\s+)/gm, " ");
                        loggerInfo(message);
                        const tradInfoVo = Object.assign(tradInfo);
                        res.status(200).json({ code: 200, message: message, data: tradInfoVo });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 发送eth2token兑换
        this.app.route(`/univ2-trade/eth2TokenSwapSendTranaction`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _swapExactETHForTokensABI = 'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts);';
                // header params
                const _privateKey: string = req.headers['private-key'] as string;
                // query params
                const _ethnode: string = req.query['ethnode'] as string;
                const _router02Addr: string = req.query['router02Addr'] as string;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                // body params
                const _amountOutMin: string = req.body.amountOutMin as string;
                const _ethInputAmount: string = req.body.ethInputAmount as string;
                const _path: string[] = req.body.path as string[];
                const _gas: number = req.body.gas as number; // 不是最小单位
                const _deadline: number = req.body.deadline as number; // 单位是秒
                // validator
                expect(_ethnode, 'ethnode 无效').to.length.gte(42);
                expect(_router02Addr, 'router02Addr 无效').to.have.lengthOf(42);
                expect(_privateKey, 'privateKey 无效').to.length.gte(42);
                expect(_amountOutMin, 'amountOutMin shoud not be empty').to.length.gt(0);
                expect(_amountOutMin, 'amountOutMin shoud not equals 0').to.be.not.equal('0');
                expect(_ethInputAmount, 'ethInputAmount shoud not be empty').to.length.gt(0);
                expect(_ethInputAmount, 'ethInputAmount shoud not equals 0').to.not.equal('0');
                expect(_path, 'path shoud length of 2').to.have.lengthOf(2);
                expect(_gas, 'gas not invalid').to.be.gt(1);
                expect(_deadline, 'deadline not invalid').to.be.gt(1);
                expect(web3.utils.isAddress(_router02Addr), 'router02Addr not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenAddr), 'tokenAddr not invalid').to.be.true;
                expect(web3.utils.isAddress(_path[0]), 'path[0] not invalid').to.be.true;
                expect(web3.utils.isAddress(_path[1]), 'path[1] not invalid').to.be.true;
                let _message = null;
                (async () => {
                    try {
                        const _tokenObject = getTokenObjectByAddress(_chainId, _tokenAddr);
                        const _token: Token = await getTokenDataByAddr(_chainId, _tokenAddr);
                        const amountOutMin: CurrencyAmount = new TokenAmount(_token, JSBI.BigInt(parseUnits(_amountOutMin, _token.decimals).toString()));
                        const ethInputAmount: CurrencyAmount = CurrencyAmount.ether(JSBI.BigInt(parseUnits(_ethInputAmount, _token.decimals).toString()));
                        const _gasPrice = parseUnits(_gas.toString(), 9).toString();
                        const _symbol = `${_tokenObject.symbol}/ETH`;
                        _message = `_tradInfo: symbol=${_symbol}, amountOutMin=${amountOutMin.toSignificant(6)}, path=${_path}, deadline=${_deadline}, value=${ethInputAmount.toSignificant(6)}, gasPrice=${_gasPrice}`;
                        loggerInfo(_message);
                        const _account = new ethers.Wallet(_privateKey).connect(ethers.getDefaultProvider('mainnet', { infura: _ethnode }));
                        const _uniswapv2 = new ethers.Contract(_router02Addr, [_swapExactETHForTokensABI], _account);
                        const _calldata: any = { ethInputAmount, gasPrice: JSBI.BigInt(_gasPrice) };
                        const tx = await _uniswapv2.sendExactETHForTokens(amountOutMin, _path, '', _deadline, _calldata);
                        loggerInfo(`Transaction Hash: ${tx.hash}`);
                        const recepit = await tx.wait();
                        loggerInfo(`Transaction was mined in block: ${recepit.blockNumber}`);
                        res.status(200).json({ code: 200, message: _message, data: recepit });
                    } catch (err) {
                        if (err.message.indexOf('invalid hexlify value') !== -1) {
                            if (err.message.indexOf(_privateKey) !== -1) {
                                res.status(200).json({ code: 400, message: `${_message}', errorMessage=私钥有误'`, data: null });
                                return;
                            }
                        }
                        next(err);
                    }
                })();
            });
        return this.app;
    }
}