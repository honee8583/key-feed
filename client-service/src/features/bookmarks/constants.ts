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
