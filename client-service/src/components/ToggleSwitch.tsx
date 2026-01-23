type ToggleSwitchProps = {
  active: boolean
  onToggle: () => void
  ariaLabel: string
  disabled?: boolean
}

export function ToggleSwitch({ active, onToggle, ariaLabel, disabled = false }: ToggleSwitchProps) {
  return (
    <button
      type="button"
      className={`relative w-[50px] h-7 rounded-full border p-[3px] inline-flex items-center transition-colors duration-200 ${
        disabled
          ? 'cursor-not-allowed opacity-50'
          : 'cursor-pointer'
      } ${
        active
          ? 'bg-gradient-to-r from-[#05df72] to-[#0fb86e] border-transparent'
          : 'border-slate-900/12 bg-slate-900/8'
      }`}
      aria-pressed={active}
      aria-label={ariaLabel}
      aria-disabled={disabled}
      onClick={disabled ? undefined : onToggle}
      disabled={disabled}
    >
      <span
        className={`w-[22px] h-[22px] rounded-full bg-white shadow-[0_4px_10px_rgba(15,23,42,0.15)] transition-transform duration-200 ${
          active ? 'translate-x-[22px]' : 'translate-x-0'
        }`}
      />
    </button>
  )
}
