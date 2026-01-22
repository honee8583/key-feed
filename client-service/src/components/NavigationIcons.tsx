export function FeedIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M1 9.00048C0.99993 8.70955 1.06333 8.4221 1.18579 8.1582C1.30824 7.89429 1.4868 7.66028 1.709 7.47248L8.709 1.47248C9.06999 1.16739 9.52736 1 10 1C10.4726 1 10.93 1.16739 11.291 1.47248L18.291 7.47248C18.5132 7.66028 18.6918 7.89429 18.8142 8.1582C18.9367 8.4221 19.0001 8.70955 19 9.00048V18.0005C19 18.5309 18.7893 19.0396 18.4142 19.4147C18.0391 19.7898 17.5304 20.0005 17 20.0005H3C2.46957 20.0005 1.96086 19.7898 1.58579 19.4147C1.21071 19.0396 1 18.5309 1 18.0005V9.00048Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  )
}

export function SearchIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  )
}

export function BookmarkIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 18.15 22.55" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M16.775 21.175L9.075 16.775L1.375 21.175V3.575C1.375 2.99152 1.60678 2.43194 2.01936 2.01936C2.43194 1.60678 2.99152 1.375 3.575 1.375H14.575C15.1585 1.375 15.7181 1.60678 16.1306 2.01936C16.5432 2.43194 16.775 2.99152 16.775 3.575V21.175Z" stroke="currentColor" strokeWidth="2.75" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  )
}

export function NotificationIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g>
        <path d="M10.268 21C10.4435 21.304 10.696 21.5565 11 21.732C11.3041 21.9075 11.6489 21.9999 12 21.9999C12.3511 21.9999 12.6959 21.9075 13 21.732C13.304 21.5565 13.5565 21.304 13.732 21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        <path d="M3.262 15.326C3.13136 15.4692 3.04515 15.6472 3.01386 15.8385C2.98256 16.0298 3.00752 16.226 3.08571 16.4034C3.16389 16.5807 3.29194 16.7316 3.45426 16.8375C3.61658 16.9434 3.80618 16.9999 4 17H20C20.1938 17.0001 20.3834 16.9438 20.5459 16.8381C20.7083 16.7324 20.8365 16.5817 20.9149 16.4045C20.9933 16.2273 21.0185 16.0311 20.9874 15.8398C20.9564 15.6485 20.8704 15.4703 20.74 15.327C19.41 13.956 18 12.499 18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 12.499 4.589 13.956 3.262 15.326Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </g>
    </svg>
  )
}

export function ProfileIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g transform="translate(5, 15)">
        <path d="M15 7V5C15 3.93913 14.5786 2.92172 13.8284 2.17157C13.0783 1.42143 12.0609 1 11 1H5C3.93913 1 2.92172 1.42143 2.17157 2.17157C1.42143 2.92172 1 3.93913 1 5V7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </g>
      <g transform="translate(8, 3)">
        <path d="M5 9C7.20914 9 9 7.20914 9 5C9 2.79086 7.20914 1 5 1C2.79086 1 1 2.79086 1 5C1 7.20914 2.79086 9 5 9Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </g>
    </svg>
  )
}
