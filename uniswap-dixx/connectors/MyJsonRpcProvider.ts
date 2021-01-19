
import { JsonRpcProvider } from '@ethersproject/providers';
import { ChainId } from '@uniswap/sdk';

export function getJsonRpcProvider(chainId: ChainId) : JsonRpcProvider {
    const _url = 'https://mainnet.infura.io/v3/4f26ec4a8ee24e2596112b826b3dba62';
    const _uname = null;
    const _passwd = null;
    if (_uname === null || _passwd === null || _uname === '' || _passwd === '') {
        return new JsonRpcProvider(_url, chainId);
    }
    //return await Fetcher.fetchTokenData(chainId, tokenAddress, getDefaultProvider(getNetwork(chainId)));
    return new JsonRpcProvider({ url: _url, user: _uname, password: _passwd }, chainId);
}