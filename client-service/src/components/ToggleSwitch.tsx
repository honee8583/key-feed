import './ToggleSwitch.css'

type ToggleSwitchProps = {
  active: boolean
  onToggle: () => void
  ariaLabel: string
}

export function ToggleSwitch({ active, onToggle, ariaLabel }: ToggleSwitchProps) {
  return (
    <button type="button" className={`toggle-switch ${active ? 'is-active' : ''}`} aria-pressed={active} aria-label={ariaLabel} onClick={onToggle}>
      <span className="toggle-switch__knob" />
    </button>
  )
}
