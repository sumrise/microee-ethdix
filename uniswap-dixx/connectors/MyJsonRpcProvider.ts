
import { JsonRpcProvider } from '@ethersproject/providers';
import { ChainId } from '@uniswap/sdk';

export function getJsonRpcProvider(chainId: ChainId) : JsonRpcProvider {
    //return await Fetcher.fetchTokenData(chainId, tokenAddress, getDefaultProvider(getNetwork(chainId)));
    return new JsonRpcProvider({url: 'https://mainnet.infura.io/v3/4f26ec4a8ee24e2596112b826b3dba62'}, chainId);
}