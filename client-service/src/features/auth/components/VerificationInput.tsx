import { useRef, type ChangeEvent, type KeyboardEvent, type ClipboardEvent } from 'react'

interface VerificationInputProps {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
}

export function VerificationInput({ value, onChange, disabled }: VerificationInputProps) {
  const inputs = useRef<(HTMLInputElement | null)[]>([])

  const handleChange = (index: number, e: ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value
    // Allow only numeric input
    if (val && !/^[0-9]$/.test(val)) return

    const newCode = value.split('')
    // If value length is less than current index (shouldn't happen if controlled correctly but safety check)
    while (newCode.length < index) newCode.push('')
    
    newCode[index] = val
    const newValue = newCode.join('').slice(0, 6)
    
    onChange(newValue)

    // Move to next input if value entered
    if (val && index < 5) {
      inputs.current[index + 1]?.focus()
    }
  }

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (!value[index] && index > 0) {
        // If current is empty, move back and delete that one?
        // Standard behavior: just move focus back.
        inputs.current[index - 1]?.focus()
         e.preventDefault() // prevent default backspace which might do nothing here
      } else if (value[index]) {
         // If has value, let default behavior happen (delete char)
         // But we need to ensure state updates. default behavior on controlled input needs onChange.
         // Actually, if we rely on onChange for deletion it works.
         // However, if we want "Backspace deletes and moves back" vs "Backspace deletes in place".
         // Let's rely on standard input behavior for deletion (will trigger onChange with empty string).
      }
    }
  }

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault()
    const pastedData = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, 6)
    if (pastedData) {
      onChange(pastedData)
      // Focus the input after the last pasted character
      const nextIndex = Math.min(pastedData.length, 5)
      inputs.current[nextIndex]?.focus()
    }
  }

  return (
    <div className="flex gap-2 justify-between w-full">
      {[0, 1, 2, 3, 4, 5].map((index) => (
        <div 
          key={index} 
          className="w-[48px] h-[52px] bg-[#1E293B] rounded-[16px] border border-transparent focus-within:border-[#2B7FFF] transition-colors flex items-center justify-center relative"
        >
          <input
            ref={(el) => { inputs.current[index] = el }}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={value[index] || ''}
            onChange={(e) => handleChange(index, e)}
            onKeyDown={(e) => handleKeyDown(index, e)}
            onPaste={handlePaste}
            disabled={disabled}
            className="bg-transparent border-none text-white text-[20px] font-bold w-full h-full text-center focus:outline-none disabled:opacity-50 p-0 caret-[#2B7FFF]"
          />
        </div>
      ))}
    </div>
  )
}
