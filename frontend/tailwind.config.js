/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          950: '#0b1220',
          900: '#121a2b',
          800: '#1c2740',
          700: '#2a3857',
          600: '#3d4f73',
          500: '#5a6f96',
          400: '#8494b5',
          300: '#b0bbd1',
          200: '#d5dbeb',
          100: '#eef1f7',
          50: '#f7f8fc',
        },
        accent: {
          DEFAULT: '#0f7a6c',
          soft: '#d8f3ee',
          deep: '#0a554b',
          glow: '#1aa891',
        },
        warn: {
          DEFAULT: '#c97816',
          soft: '#fff1dc',
        },
      },
      fontFamily: {
        display: ['"Sora"', 'system-ui', 'sans-serif'],
        sans: ['"IBM Plex Sans"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        panel: '0 18px 40px -28px rgba(11, 18, 32, 0.45)',
      },
      backgroundImage: {
        mesh: 'radial-gradient(ellipse at 12% 8%, rgba(26,168,145,0.16), transparent 42%), radial-gradient(ellipse at 88% 0%, rgba(201,120,22,0.10), transparent 36%), linear-gradient(180deg, #f4f6fb 0%, #e8edf6 100%)',
      },
    },
  },
  plugins: [],
};
