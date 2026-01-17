import { type ComponentProps } from 'react'

export type IconType =
  | 'folder'
  | 'star'
  | 'clock'
  | 'heart'
  | 'bookmark'
  | 'tag'
  | 'archive'
  | 'folder-open'

export type ColorType =
  | '#2b7fff'
  | '#ad46ff'
  | '#00c950'
  | '#fb2c36'
  | '#ff6900'
  | '#f6339a'
  | '#f0b100'
  | '#6a7282'

export const ICONS: IconType[] = [
  'folder',
  'star',
  'clock',
  'heart',
  'bookmark',
  'tag',
  'archive',
  'folder-open',
]

export const COLORS: ColorType[] = [
  '#2b7fff',
  '#ad46ff',
  '#00c950',
  '#fb2c36',
  '#ff6900',
  '#f6339a',
  '#f0b100',
  '#6a7282',
]

export const ICON_LABELS: Record<IconType, string> = {
  folder: '폴더',
  star: '별',
  clock: '시계',
  heart: '하트',
  bookmark: '북마크',
  tag: '태그',
  archive: '보관함',
  'folder-open': '열린 폴더',
}

type Props = ComponentProps<'svg'> & {
  icon?: string
}

export function BookmarkFolderIcon({ icon, ...props }: Props) {
  // 기본값 처리 및 유효하지 않은 아이콘 처리
  const validIcon = (ICONS.includes(icon as IconType) ? icon : 'folder') as IconType

  switch (validIcon) {
    case 'folder':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M2.5 5.83333C2.5 4.91286 3.24619 4.16667 4.16667 4.16667H7.08925C7.53857 4.16667 7.96634 4.34226 8.28141 4.65734L9.82259 6.19851C10.1377 6.51359 10.5654 6.68917 11.0148 6.68917H15.8333C16.7538 6.68917 17.5 7.43537 17.5 8.35583V14.1667C17.5 15.0871 16.7538 15.8333 15.8333 15.8333H4.16667C3.24619 15.8333 2.5 15.0871 2.5 14.1667V5.83333Z"
            stroke="currentColor"
            strokeWidth="1.5"
          />
        </svg>
      )
    case 'star':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M10 2.5L12.245 7.755L18 8.5L14 12.755L15 18.5L10 15.755L5 18.5L6 12.755L2 8.5L7.755 7.755L10 2.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
        </svg>
      )
    case 'clock':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <circle cx="10" cy="10" r="7.5" stroke="currentColor" strokeWidth="1.5" />
          <path d="M10 6V10L13 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      )
    case 'heart':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M10 17.5C10 17.5 2.5 12.5 2.5 7.5C2.5 4.5 4.5 2.5 7 2.5C8.5 2.5 10 3.5 10 3.5C10 3.5 11.5 2.5 13 2.5C15.5 2.5 17.5 4.5 17.5 7.5C17.5 12.5 10 17.5 10 17.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
        </svg>
      )
    case 'bookmark':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M5 3.75C5 3.05964 5.55964 2.5 6.25 2.5H13.75C14.4404 2.5 15 3.05964 15 3.75V17.5L10 14.1667L5 17.5V3.75Z"
            stroke="currentColor"
            strokeWidth="1.5"
          />
        </svg>
      )
    case 'tag':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M2.5 7.5L7.5 2.5L17.5 12.5L12.5 17.5L2.5 7.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinejoin="round"
          />
          <circle cx="8.75" cy="8.75" r="1.25" fill="currentColor" />
        </svg>
      )
    case 'archive':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M3.75 5.83333H16.25M3.75 5.83333V15.8333C3.75 16.2936 4.1231 16.6667 4.58333 16.6667H15.4167C15.8769 16.6667 16.25 16.2936 16.25 15.8333V5.83333M3.75 5.83333L5 3.33333H15L16.25 5.83333M8.33333 9.16667H11.6667"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </svg>
      )
    case 'folder-open':
      return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" {...props}>
          <path
            d="M2.5 5.83333C2.5 4.91286 3.24619 4.16667 4.16667 4.16667H7.08925C7.53857 4.16667 7.96634 4.34226 8.28141 4.65734L9.82259 6.19851C10.1377 6.51359 10.5654 6.68917 11.0148 6.68917H15.8333C16.7538 6.68917 17.5 7.43537 17.5 8.35583V9.16667M2.5 14.1667L4.16667 9.16667H18.3333L16.6667 14.1667H2.5Z"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
        </svg>
      )
    default:
      return null
  }
}
