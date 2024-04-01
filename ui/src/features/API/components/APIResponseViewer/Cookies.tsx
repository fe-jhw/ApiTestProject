import { Error, Info, Table } from '@/components'
import { useObjectEntries } from '@/hooks'
import { FetchApiResponse } from 'api-types'
import { FC, useMemo } from 'react'
import { tableWrapperScrollableCss } from './tableWrapperScrollableCss'

const columns = [
  'name',
  'value',
  'comment',
  'commentURL',
  'domain',
  'maxAge',
  'path',
  'portlist',
  'secure',
  'httpOnly',
  'version',
  'discard',
]

function parseCookies(cookies: FetchApiResponse['cookies']): Array<Array<string | number>> {
  return cookies.map(cookie => {
    return columns.map(column => cookie[column])
  })
}

interface CookiesProps {
  cookies: FetchApiResponse['cookies'] | null
}

export const Cookies: FC<CookiesProps> = ({ cookies }) => {
  if (cookies == null || cookies?.length === 0) {
    return <Info message={'Cookie가 없습니다.'} />
  }
  const parsedCookies = parseCookies(cookies)

  return (
    <div css={tableWrapperScrollableCss}>
      <Table columns={columns} data={parsedCookies} />
    </div>
  )
}
